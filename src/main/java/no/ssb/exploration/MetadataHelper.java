package no.ssb.exploration;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.protobuf.InvalidProtocolBufferException;
import com.google.protobuf.Message;
import com.google.protobuf.util.JsonFormat;
import no.ssb.dapla.dataset.api.DatasetMeta;
import no.ssb.dapla.dataset.doc.model.lineage.Dataset;
import no.ssb.dapla.dataset.doc.model.simple.Record;
import no.ssb.exploration.model.GsimBuilder;
import no.ssb.exploration.model.UnitDataStructure;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;

import static java.util.Optional.ofNullable;

/**
 * Not thread safe! This class must be used by a single thread, or external synchronization must be performed to
 * avoid race-conditions or memory-visibility issues.
 */
public class MetadataHelper {

    final ObjectMapper mapper;
    final JsonNode dataNode;
    DatasetMeta datasetMeta;
    ZonedDateTime datasetVersionTimestamp;
    Record datasetDocRootRecord;
    SimpleToGsim simpleToGsim;
    LDSObject unitDataStructureLdsObject;
    LDSObject datasetLdsObject;
    List<LDSObject> logicalRecordsAndInstanceVariables;
    LDSObject lineageDataset;
    List<LDSObject> lineageFields;

    public MetadataHelper(ObjectMapper mapper, JsonNode dataNode) {
        this.mapper = mapper;
        this.dataNode = dataNode;
    }

    /**
     * @return true if the data is valid
     */
    public boolean validate() {
        return dataNode.has("dataset-meta");
    }

    public DatasetMeta datasetMeta() {
        if (datasetMeta == null) {
            try {
                JsonNode datasetMetaNode = dataNode.get("dataset-meta");
                String metadataJson;
                metadataJson = mapper.writeValueAsString(datasetMetaNode);
                // convert json string to DatasetMeta protobuf instance
                Message.Builder builder = (Message.Builder) DatasetMeta.class
                        .getMethod("newBuilder", (Class<?>[]) null)
                        .invoke(null);
                JsonFormat.parser().merge(metadataJson, builder);
                datasetMeta = (DatasetMeta) builder.build();
            } catch (JsonProcessingException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InvalidProtocolBufferException e) {
                throw new RuntimeException(e);
            }
        }
        return datasetMeta;
    }

    public ZonedDateTime versionTimestamp() {
        if (datasetVersionTimestamp == null) {
            datasetVersionTimestamp = ZonedDateTime.ofInstant(Instant.ofEpochMilli(Long.parseLong(datasetMeta.getId().getVersion())), ZoneOffset.UTC);
        }
        return datasetVersionTimestamp;
    }

    /**
     * @return the resource-id to be used by the LDS Dataset instance
     */
    public String datasetId() {
        return createDatasetId(datasetMeta().getId().getPath());
    }

    private static String createDatasetId(String dataSetPath) {
        String path = dataSetPath.substring(1); // Remove first slash
        return path.replace("/", ".");
    }

    public Record datasetDocRootRecord() {
        if (datasetDocRootRecord == null && dataNode.has("dataset-doc")) {
            try {
                datasetDocRootRecord = mapper.treeToValue(dataNode.get("dataset-doc"), Record.class);
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return datasetDocRootRecord;
    }

    public SimpleToGsim simpleToGsim() {
        if (simpleToGsim == null) {
            simpleToGsim = new SimpleToGsim(datasetDocRootRecord(), datasetMeta().getId().getPath(), versionTimestamp())
                    .createdBy(datasetMeta().getCreatedBy());
        }
        return simpleToGsim;
    }

    public LDSObject unitDataStructure() {
        if (unitDataStructureLdsObject == null) {
            UnitDataStructure unitDataStructure = simpleToGsim().createUnitDataStructure();
            if (unitDataStructure == null) {
                return null;
            }
            unitDataStructureLdsObject = new LDSObject("UnitDataStructure", unitDataStructure.getId(), versionTimestamp(), () -> unitDataStructure);
        }
        return unitDataStructureLdsObject;
    }

    public LDSObject unitDataSet() {
        if (datasetLdsObject == null) {
            GsimBuilder.UnitDatasetBuilder unitDatasetBuilder = GsimBuilder.create()
                    .id(datasetId())
                    .languageCode("nb")
                    .createdBy(datasetMeta().getCreatedBy())
                    .name(ofNullable(datasetDocRootRecord()).map(Record::getName).orElse(""))
                    .description(ofNullable(datasetDocRootRecord()).map(Record::getDescription).orElse(""))
                    .addProperty("administrativeStatus", "DRAFT") // TODO user should decide when writing data or decided by architecture
                    .addProperty("createdDate", versionTimestamp().toString())
                    .addProperty("validFrom", versionTimestamp().toString())
                    .addProperty("version", "1.0.0")
                    .addProperty("versionValidFrom", versionTimestamp().toString())
                    .unitDataSet()
                    .temporalityType(DatasetTools.toTemporality("TODO")) // TODO: get this from correct place
                    .dataSetState(DatasetTools.toExplorationState(datasetMeta().getState()))
                    .dataSourcePath(datasetMeta().getId().getPath());

            // TODO we should always have this, even without dataset-doc. This could be based on e.g. avro schema
            ofNullable(unitDataStructure()).ifPresent(uds -> unitDatasetBuilder.unitDataStructure(uds.id));

            ofNullable(lineageDataset()).ifPresent(ld -> unitDatasetBuilder.lineage(ld.link()));

            datasetLdsObject = new LDSObject("UnitDataSet", datasetId(), versionTimestamp(), unitDatasetBuilder::build);
        }

        return datasetLdsObject;
    }

    public List<LDSObject> logicalRecordsAndInstanceVariables() {
        if (logicalRecordsAndInstanceVariables == null) {
            logicalRecordsAndInstanceVariables = simpleToGsim().createLogicalRecordsAndInstanceVariables();
        }
        return logicalRecordsAndInstanceVariables;
    }

    public LDSObject lineageDataset() {
        if (lineageDataset == null) {
            JsonNode lineageDocNode = dataNode.get("dataset-lineage");
            if (lineageDocNode == null) {
                return null;
            }
            try {
                Dataset dataset = mapper.treeToValue(lineageDocNode, Dataset.class);
                LineageTemplateToExplorationLineage toExplorationLineage = new LineageTemplateToExplorationLineage(dataset, datasetLdsObject);
                lineageDataset = toExplorationLineage.createLineageDatasetLdsObject();
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return lineageDataset;
    }

    public List<LDSObject> lineageFields() {
        if (lineageFields == null) {
            lineageFields = new LinkedList<>();
        }
        return lineageFields;
    }
}
