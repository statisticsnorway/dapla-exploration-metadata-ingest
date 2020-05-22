package no.ssb.dapla.exploration_metadata_ingest;

import com.google.cloud.pubsub.v1.Publisher;
import com.google.protobuf.ByteString;
import com.google.pubsub.v1.PubsubMessage;
import io.helidon.config.Config;
import io.helidon.media.common.DefaultMediaSupport;
import io.helidon.media.jackson.common.JacksonSupport;
import io.helidon.webclient.WebClient;
import io.helidon.webserver.WebServer;
import no.ssb.pubsub.PubSub;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

import static io.helidon.config.ConfigSources.classpath;

public class ExplorationMetadataIngestApplicationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ExplorationMetadataIngestApplicationTest.class);

    static {
        ExplorationMetadataIngestApplication.initLogging();
    }

    static ExplorationMetadataIngestApplication application;
    static WebServer webServer;

    @BeforeAll
    public static void startTheServer() {
        Config config = Config
                .builder(classpath("application-dev.yaml"),
                        classpath("application.yaml"))
                .metaConfig()
                .build();
        long webServerStart = System.currentTimeMillis();
        application = new ExplorationMetadataIngestApplication(config);
        webServer = application.get(WebServer.class);
        webServer.start().toCompletableFuture()
                .thenAccept(webServer -> {
                    long duration = System.currentTimeMillis() - webServerStart;
                    LOG.info("Server started in {} ms, listening at port {}", duration, webServer.port());
                })
                .orTimeout(5, TimeUnit.SECONDS)
                .join();
    }

    @AfterAll
    public static void stopServer() {
        if (webServer != null) {
            webServer.shutdown()
                    .toCompletableFuture()
                    .orTimeout(10, TimeUnit.SECONDS)
                    .join();
        }
    }

    @Test
    public void testHelloWorld() {
        WebClient webClient = WebClient.builder()
                .baseUri("http://localhost:" + webServer.port())
                .addMediaSupport(DefaultMediaSupport.create(true))
                .addMediaSupport(JacksonSupport.create())
                .build();

        webClient.put()
                .path("/pipe/trigger")
                .submit()
                .thenAccept(response -> Assertions.assertEquals(200, response.status().code()))
                .exceptionally(throwable -> {
                    Assertions.fail(throwable);
                    return null;
                })
                .toCompletableFuture()
                .orTimeout(60, TimeUnit.SECONDS)
                .join();
    }

    @Test
    public void thatPubSubMessageIsConsumed() throws InterruptedException {
        Config config = application.get(Config.class);
        PubSub pubSub = application.get(PubSub.class);
        Config upstream = config.get("pubsub.upstream");
        String projectId = upstream.get("projectId").asString().get();
        String topic = upstream.get("topic").asString().get();
        Publisher publisher = pubSub.getPublisher(projectId, topic);
        try {
            publisher.publish(PubsubMessage.newBuilder()
                    .setData(ByteString.copyFromUtf8("{\"test\":\"me\"}"))
                    .build());
        } finally {
            publisher.shutdown();
        }

        DatasetUpstreamGooglePubSubIntegration handler = application.get(DatasetUpstreamGooglePubSubIntegration.class);
        while (handler.counter.get() <= 0) {
            Thread.sleep(100);
        }
    }
}