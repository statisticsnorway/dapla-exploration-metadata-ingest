package no.ssb.dapla.exploration_metadata_ingest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.cloud.pubsub.v1.AckReplyConsumer;
import com.google.cloud.pubsub.v1.MessageReceiver;
import com.google.cloud.pubsub.v1.Subscriber;
import com.google.common.util.concurrent.MoreExecutors;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import com.google.pubsub.v1.PubsubMessage;
import io.helidon.config.Config;
import no.ssb.dapla.dataset.api.DatasetMeta;
import no.ssb.dapla.dataset.doc.model.lineage.Dataset;
import no.ssb.dapla.dataset.doc.model.simple.Record;
import no.ssb.exploration.DatasetTools;
import no.ssb.exploration.GsimBuilder;
import no.ssb.exploration.LineageTemplateToExplorationLineage;
import no.ssb.exploration.SimpleToGsim;
import no.ssb.exploration.model.LDSObject;
import no.ssb.exploration.model.PersistenceProvider;
import no.ssb.exploration.model.UnitDataSet;
import no.ssb.exploration.model.UnitDataStructure;
import no.ssb.pubsub.PubSub;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
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
            if (!dataNode.has("dataset-meta")) {
                LOG.warn("Message IGNORED. Received message with invalid protocol. Missing 'dataset-meta' field in json-document.");
                consumer.ack();
                return;
            }

            LOG.debug("RECEIVED metadata:\n{}", dataNode.toPrettyString());

            JsonNode datasetMetaNode = dataNode.get("dataset-meta");
            String metadataJson = mapper.writeValueAsString(datasetMetaNode);

            // convert json string to DatasetMeta protobuf instance
            Message.Builder builder = (Message.Builder) DatasetMeta.class
                    .getMethod("newBuilder", (Class<?>[]) null)
                    .invoke(null);
            JsonFormat.parser().merge(metadataJson, builder);
            DatasetMeta datasetMeta = (DatasetMeta) builder.build();

            String path = datasetMeta.getId().getPath();

            ZonedDateTime datasetVersionTimestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(datasetMeta.getId().getVersion())), ZoneOffset.UTC);

            final Map<String, List<LDSObject>> ldsObjectsByType = new LinkedHashMap<>();

            String id = DatasetTools.createDatasetId(datasetMeta.getId().getPath());
            GsimBuilder.BaseBuilder unitDatasetBaseBuilder = GsimBuilder.create()
                    .id(id)
                    .languageCode("nb")
                    .createdBy(datasetMeta.getCreatedBy())
                    .addProperty("administrativeStatus", "DRAFT") // TODO user should decide when writing data or decided by architecture
                    .addProperty("createdDate", datasetVersionTimestamp.toString())
                    .addProperty("validFrom", datasetVersionTimestamp.toString())
                    .addProperty("version", "1.0.0")
                    .addProperty("versionValidFrom", datasetVersionTimestamp.toString());

            String parentUri = dataNode.get("parentUri").textValue();
            JsonNode datasetDocNode = dataNode.get("dataset-doc");
            GsimBuilder.UnitDatasetBuilder unitDatasetBuilder;
            if (datasetDocNode != null) {
                unitDatasetBuilder = datasetDocToLDS(ldsObjectsByType, datasetMeta, path, datasetVersionTimestamp, unitDatasetBaseBuilder, datasetDocNode);
            } else {
                unitDatasetBuilder = unitDatasetBaseBuilder.unitDataSet();
            }

            UnitDataSet unitDataset = unitDatasetBuilder
                    .temporalityType(DatasetTools.toTemporality("TODO")) // TODO: get this from correct place
                    .dataSetState(DatasetTools.toExplorationState(datasetMeta.getState()))
                    .dataSourcePath(datasetMeta.getId().getPath())
                    .build();

            LDSObject datasetLdsObject = new LDSObject("UnitDataSet", unitDataset.getId(), datasetVersionTimestamp, () -> unitDataset);
            ldsObjectsByType.computeIfAbsent("UnitDataSet", k -> new LinkedList<>())
                    .add(datasetLdsObject);

            JsonNode lineageDocNode = dataNode.get("dataset-lineage");
            lineageToLDS(ldsObjectsByType, datasetLdsObject, lineageDocNode);

            saveAllToExplorationLDS(ldsObjectsByType);

            consumer.ack();
            counter.incrementAndGet();

        } catch (Throwable t) {
            String json = dataNode != null ? dataNode.toPrettyString() : "null";
            LOG.error("Error while processing message, waiting for ack deadline before re-delivery\njson:{}", json, t);
        }
    }

    private GsimBuilder.UnitDatasetBuilder datasetDocToLDS(Map<String, List<LDSObject>> ldsObjectsByType, DatasetMeta datasetMeta, String path, ZonedDateTime datasetVersionTimestamp, GsimBuilder.BaseBuilder unitDatasetBaseBuilder, JsonNode datasetDocNode) throws com.fasterxml.jackson.core.JsonProcessingException {
        GsimBuilder.UnitDatasetBuilder unitDatasetBuilder;
        Record record = mapper.treeToValue(datasetDocNode, Record.class);

        SimpleToGsim simpleToGsim = new SimpleToGsim(record, path, datasetVersionTimestamp)
                .createdBy(datasetMeta.getCreatedBy());

        UnitDataStructure unitDataStructure = simpleToGsim.createUnitDataStructure(record);
        ldsObjectsByType.computeIfAbsent("UnitDataStructure", k -> new LinkedList<>())
                .add(new LDSObject("UnitDataStructure", unitDataStructure.getId(), datasetVersionTimestamp, () -> unitDataStructure));

        unitDatasetBuilder = unitDatasetBaseBuilder
                .name(record.getName())
                .description(record.getDescription())
                .unitDataSet()
                .unitDataStructure(unitDataStructure.getId()); // TODO we should always have this, even without dataset-doc. This could be based on e.g. avro schema

        simpleToGsim.createGsimObjects(ldsObjectsByType);
        return unitDatasetBuilder;
    }

    private LDSObject lineageToLDS(Map<String, List<LDSObject>> ldsObjectsByType, LDSObject datasetLdsObject, JsonNode lineageDocNode) {
        if (lineageDocNode == null) {
            return null;
        }
        try {
            LOG.info("dataset-lineage");
            Dataset dataset = mapper.treeToValue(lineageDocNode, Dataset.class);

            String json = lineageDocNode.toPrettyString();
            LOG.info(json);

            LineageTemplateToExplorationLineage toExplorationLineage = new LineageTemplateToExplorationLineage(dataset, datasetLdsObject);
            LDSObject lineageDatasetLdsObject = toExplorationLineage.createLdsLinageObjects(ldsObjectsByType);
            lineageDatasetLdsObject.link();

            return lineageDatasetLdsObject;
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
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
