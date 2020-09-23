package no.ssb.dapla.exploration_metadata_ingest;


import ch.qos.logback.classic.util.ContextInitializer;
import io.helidon.config.Config;
import io.helidon.health.HealthSupport;
import io.helidon.health.checks.HealthChecks;
import io.helidon.media.common.DefaultMediaSupport;
import io.helidon.media.jackson.server.JacksonSupport;
import io.helidon.metrics.MetricsSupport;
import io.helidon.webclient.WebClient;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerConfiguration;
import io.helidon.webserver.WebServer;
import io.helidon.webserver.WebTracingConfig;
import io.helidon.webserver.accesslog.AccessLogSupport;
import no.ssb.exploration.model.PersistenceProvider;
import no.ssb.pubsub.EmulatorPubSub;
import no.ssb.pubsub.PubSub;
import no.ssb.pubsub.RealPubSub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.LogManager;

import static io.helidon.config.ConfigSources.classpath;
import static io.helidon.config.ConfigSources.file;

public class ExplorationMetadataIngestApplication {

    private static final Logger LOG;

    static {
        String logbackConfigurationFile = System.getenv("LOGBACK_CONFIGURATION_FILE");
        if (logbackConfigurationFile != null) {
            System.setProperty(ContextInitializer.CONFIG_FILE_PROPERTY, logbackConfigurationFile);
        }
        LogManager.getLogManager().reset();
        SLF4JBridgeHandler.removeHandlersForRootLogger();
        SLF4JBridgeHandler.install();
        LOG = LoggerFactory.getLogger(ExplorationMetadataIngestApplication.class);
    }

    public static void initLogging() {
    }

    public static Config createDefaultConfig() {
        Config.Builder builder = Config.builder();
        String overrideFile = System.getenv("HELIDON_CONFIG_FILE");
        if (overrideFile != null) {
            builder.addSource(file(overrideFile).optional());
        }
        builder.addSource(file("conf/application.yaml").optional());
        builder.addSource(classpath("application.yaml"));
        return builder.build();
    }

    public static void main(final String[] args) throws IOException {
        ExplorationMetadataIngestApplication app = new ExplorationMetadataIngestApplication(createDefaultConfig());

        // Try to start the server. If successful, print some info and arrange to
        // print a message at shutdown. If unsuccessful, print the exception.
        app.get(WebServer.class).start()
                .thenAccept(ws -> {
                    System.out.println(
                            "WEB server is up! http://localhost:" + ws.port() + "/greet");
                    ws.whenShutdown().thenRun(()
                            -> System.out.println("WEB server is DOWN. Good bye!"));
                })
                .exceptionally(t -> {
                    System.err.println("Startup failed: " + t.getMessage());
                    t.printStackTrace(System.err);
                    return null;
                });
    }

    private final Map<Class<?>, Object> instanceByType = new ConcurrentHashMap<>();

    ExplorationMetadataIngestApplication(Config config) {
        put(Config.class, config);

        HealthSupport health = HealthSupport.builder()
                .addLiveness(HealthChecks.healthChecks())   // Adds a convenient set of checks
                .build();
        MetricsSupport metrics = MetricsSupport.create();

        ExplorationMetadataIngestService explorationMetadataIngestService = new ExplorationMetadataIngestService();
        put(ExplorationMetadataIngestService.class, explorationMetadataIngestService);

        if (config.get("pubsub.enabled").asBoolean().orElse(false)) {
            LOG.info("Running with PubSub enabled");

            PubSub pubSub = createPubSub(config.get("pubsub"));
            put(PubSub.class, pubSub);

            LOG.info("Created PubSub of class type: " + pubSub.getClass().getName());

            if (config.get("pubsub.admin").asBoolean().orElse(false)) {
                LOG.info("Admin of topics and subscriptions enabled, running initializer");
                DatasetUpstreamGooglePubSubIntegrationInitializer.initializeTopicsAndSubscriptions(config.get("pubsub.upstream"), pubSub);
                LOG.info("Initialization of topics and subscriptions complete");
            }

            Config targetConfig = config.get("pipe.target");

            PersistenceProvider persistenceProvider;
            if (targetConfig.get("mock").asBoolean().orElse(false)) {
                persistenceProvider = identifiableArtefact -> {
                };
            } else {
                String scheme = targetConfig.get("scheme").asString().get();
                String host = targetConfig.get("host").asString().get();
                String namespace = targetConfig.get("namespace").asString().get();
                String source = targetConfig.get("source").asString().get();
                int port = targetConfig.get("port").asInt().get();
                URI ldsBaseUri;
                try {
                    ldsBaseUri = new URI(scheme, null, host, port, "/" + namespace, "source=" + source, null);
                } catch (URISyntaxException e) {
                    throw new RuntimeException(e);
                }
                WebClient webClient = WebClient.builder()
                        .addMediaSupport(DefaultMediaSupport.create(true))
                        .addMediaSupport(io.helidon.media.jackson.common.JacksonSupport.create())
                        .baseUri(ldsBaseUri)
                        .build();
                persistenceProvider = new ExplorationLdsHttpProvider(webClient);
            }
            DatasetUpstreamGooglePubSubIntegration datasetUpstreamSubscriber = new DatasetUpstreamGooglePubSubIntegration(config.get("pubsub.upstream"), pubSub, persistenceProvider);
            put(DatasetUpstreamGooglePubSubIntegration.class, datasetUpstreamSubscriber);

            LOG.info("Subscribed upstream");
        }

        WebServer server = WebServer.create(ServerConfiguration.create(config.get("server")), Routing.builder()
                .register(AccessLogSupport.create(config.get("server.access-log")))
                .register(WebTracingConfig.create(config.get("tracing")))
                .register(JacksonSupport.create())
                .register(health)  // "/health"
                .register(metrics) // "/metrics"
                .register("/pipe", explorationMetadataIngestService)
                .build());
        put(WebServer.class, server);
    }

    public static PubSub createPubSub(Config config) {
        boolean useEmulator = config.get("use-emulator").asBoolean().orElse(false);
        if (useEmulator) {
            Config emulatorConfig = config.get("emulator");
            String host = emulatorConfig.get("host").asString().get();
            int port = emulatorConfig.get("port").asInt().get();
            return new EmulatorPubSub(host, port);
        } else {
            String configuredProviderChoice = config.get("credential-provider").asString().orElse("default");
            if ("service-account".equalsIgnoreCase(configuredProviderChoice)) {
                LOG.info("PubSub running with the service-account google credentials provider");
                String serviceAccountKeyPath = config.get("credentials.service-account.path").asString().orElse(null);
                return RealPubSub.createWithServiceAccountKeyCredentials(serviceAccountKeyPath);
            } else if ("compute-engine".equalsIgnoreCase(configuredProviderChoice)) {
                LOG.info("PubSub running with the compute-engine google credentials provider");
                return RealPubSub.createWithComputeEngineCredentials();
            } else { // default
                LOG.info("PubSub running with the default google credentials provider");
                return RealPubSub.createWithDefaultCredentials();
            }
        }
    }

    public <T> T put(Class<T> clazz, T instance) {
        return (T) instanceByType.put(clazz, instance);
    }

    public <T> T get(Class<T> clazz) {
        return (T) instanceByType.get(clazz);
    }
}
