package no.ssb.dapla.exploration_metadata_ingest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.pubsub.v1.PubsubMessage;
import io.helidon.config.Config;
import no.ssb.exploration.MetadataHelper;
import no.ssb.exploration.model.LDSObject;
import no.ssb.exploration.model.PersistenceProvider;
import no.ssb.pubsub.PubSub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicLong;

public class DatasetUpstreamGooglePubSubIntegration implements MessageReceiver {

    private static final Logger LOG = LoggerFactory.getLogger(DatasetUpstreamGooglePubSubIntegration.class);

    final PubSub pubSub;
    final Subscriber subscriber;
    final ObjectMapper mapper = new ObjectMapper();
    final AtomicLong counter = new AtomicLong(0);
    final PersistenceProvider persistenceProvider;

    public DatasetUpstreamGooglePubSubIntegration(Config pubSubUpstreamConfig, PubSub pubSub, PersistenceProvider persistenceProvider) {
        this.pubSub = pubSub;
        this.persistenceProvider = persistenceProvider;

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
        JsonNode dataNode = null;
        try {
            try (InputStream inputStream = message.getData().newInput()) {
                dataNode = mapper.readTree(inputStream);
            }
            MetadataHelper helper = new MetadataHelper(mapper, dataNode);
            if (!helper.validate()) {
                LOG.warn("Message IGNORED. Received message with invalid protocol. Missing 'dataset-meta' field in json-document.");
                consumer.ack();
                return;
            }

            // String parentUri = dataNode.get("parentUri").textValue();

            LOG.debug("RECEIVED metadata:\n{}", dataNode.toPrettyString());

            final Map<String, List<LDSObject>> ldsObjectsByType = new LinkedHashMap<>();

            add(ldsObjectsByType, helper.unitDataStructure());
            add(ldsObjectsByType, helper.unitDataSet());
            add(ldsObjectsByType, helper.logicalRecordsAndInstanceVariables());
            add(ldsObjectsByType, helper.lineageDataset());
            add(ldsObjectsByType, helper.lineageFields());

            saveAllToExplorationLDS(ldsObjectsByType);

            consumer.ack();
            counter.incrementAndGet();

        } catch (Throwable t) {
            String json = dataNode != null ? dataNode.toPrettyString() : "null";
            LOG.error("Error while processing message, waiting for ack deadline before re-delivery\njson:{}", json, t);
        }
    }

    private void add(Map<String, List<LDSObject>> ldsObjectsByType, LDSObject ldsObject) {
        if (ldsObject != null) {
            ldsObjectsByType.computeIfAbsent(ldsObject.type, k -> new LinkedList<>()).add(ldsObject);
        }
    }

    private void add(Map<String, List<LDSObject>> ldsObjectsByType, Iterable<LDSObject> ldsObjects) {
        for (LDSObject ldsObject : ldsObjects) {
            ldsObjectsByType.computeIfAbsent(ldsObject.type, k -> new LinkedList<>()).add(ldsObject);
        }
    }

    private void saveAllToExplorationLDS(Map<String, List<LDSObject>> ldsObjectsByType) {
        // persist all LDS objects
        for (Map.Entry<String, List<LDSObject>> typeEntries : ldsObjectsByType.entrySet()) {
            for (LDSObject ldsObject : typeEntries.getValue()) {
                persistenceProvider.save(ldsObject);
            }
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
