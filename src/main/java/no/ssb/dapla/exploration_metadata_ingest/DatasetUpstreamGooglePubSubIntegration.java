package no.ssb.dapla.exploration_metadata_ingest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.pubsub.v1.PubsubMessage;
import io.helidon.config.Config;
import io.helidon.webclient.WebClient;
import no.ssb.pubsub.PubSub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class DatasetUpstreamGooglePubSubIntegration implements MessageReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetUpstreamGooglePubSubIntegration.class);

    final PubSub pubSub;
    final Subscriber subscriber;
    final WebClient explorationLdsWebClient;
    final ObjectMapper mapper = new ObjectMapper();
    final AtomicLong counter = new AtomicLong(0);

    public DatasetUpstreamGooglePubSubIntegration(Config pubSubUpstreamConfig, PubSub pubSub, WebClient explorationLdsWebClient) {
        this.pubSub = pubSub;
        this.explorationLdsWebClient = explorationLdsWebClient;

        String projectId = pubSubUpstreamConfig.get("projectId").asString().get();
        String topicName = pubSubUpstreamConfig.get("topic").asString().get();
        String subscriptionName = pubSubUpstreamConfig.get("subscription").asString().get();

        LOG.info("Using upstream topic: {}", topicName);
        LOG.info("Using upstream subscription: {}", subscriptionName);
        LOG.info("Creating subscriber");
        subscriber = pubSub.getSubscriber(projectId, subscriptionName, this);
        subscriber.addListener(
                new Subscriber.Listener() {
                    public void failed(Subscriber.State from, Throwable failure) {
                        LOG.error(String.format("Error with subscriber on subscription '%s' and topic '%s'", subscriptionName, topicName), failure);
                    }
                },
                MoreExecutors.directExecutor());
        LOG.info("Subscriber async pull starting...");
        subscriber.startAsync().awaitRunning();
        LOG.info("Subscriber async pull is now running.");
    }

    @Override
    public void receiveMessage(PubsubMessage message, AckReplyConsumer consumer) {
        try {
            String parentUri = message.getAttributesMap().get("parentUri");
            String json = message.getData().toStringUtf8();
            JsonNode jsonNode = mapper.readTree(json);

            System.out.printf("Exploration INGEST: Received metadata:%n%s%n", jsonNode);

            // TODO transform to Exploration LDS format and put data

            /*
            WebClientResponse response = explorationLdsWebClient.put()
                    .path("/EntityType/resource-id/version") // TODO replace with resource path here
                    .readTimeout(30, ChronoUnit.SECONDS)
                    .connectTimeout(30, ChronoUnit.SECONDS)
                    .submit()
                    .toCompletableFuture()
                    .join();

            if (!Http.ResponseStatus.Family.SUCCESSFUL.equals(response.status().family())) {
                throw new RuntimeException(String.format("Got response code %d from Exploration LDS with reason: %s",
                        response.status().code(), response.status().reasonPhrase()));
            }
            */

            counter.incrementAndGet();

        } catch (RuntimeException | Error e) {
            LOG.error("Error while processing message, waiting for ack deadline before re-delivery", e);
            throw e;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    public void close() {
        subscriber.stopAsync();
        try {
            subscriber.awaitTerminated(10, TimeUnit.SECONDS);
        } catch (TimeoutException e) {
            throw new RuntimeException(e);
        }
    }
}
