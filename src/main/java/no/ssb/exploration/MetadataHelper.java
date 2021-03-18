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
import no.ssb.exploration.model.LineageDataset;
import no.ssb.exploration.model.LineageField;
import no.ssb.exploration.model.UnitDataSet;
import no.ssb.exploration.model.UnitDataStructure;
import org.apache.avro.Schema;

import java.lang.reflect.InvocationTargetException;
import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Optional.of;
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
    SimpleToExploration simpleToGsim;
    LDSObject unitDataStructureLdsObject;
    LDSObject datasetLdsObject;
    List<LDSObject> logicalRecordsAndInstanceVariables;
    LDSObject lineageDataset;
    List<LDSObject> lineageFields;
    LineageTemplateToExplorationLineage toExplorationLineage;

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
        return DatasetTools.datasetId(datasetMeta().getId().getPath());
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

    public SimpleToExploration simpleToGsim() {
        if (simpleToGsim == null) {
            simpleToGsim = new SimpleToExploration(datasetDocRootRecord(), datasetMeta().getId().getPath(), versionTimestamp())
                    .createdBy(datasetMeta().getCreatedBy());
        }
        return simpleToGsim;
    }

    public LDSObject unitDataStructure() {
        if (unitDataStructureLdsObject == null) {
            UnitDataStructure unitDataStructure = simpleToGsim().createUnitDataStructure();
            unitDataStructureLdsObject = new LDSObject("UnitDataStructure", unitDataStructure.getId(), versionTimestamp(), () -> unitDataStructure);
        }
        return unitDataStructureLdsObject;
    }

    public LDSObject unitDataSet() {
        if (datasetLdsObject == null) {
            UnitDataSet unitDataset = GsimBuilder.create()
                    .id(datasetId())
                    .languageCode("nb")
                    .createdBy(datasetMeta().getCreatedBy())
                    .name(ofNullable(datasetDocRootRecord()).map(Record::getName).orElse(""))
                    .description(ofNullable(datasetDocRootRecord()).map(Record::getDescription).orElse(""))
                    .addProperty("administrativeStatus", "DRAFT") // TODO user should decide when writing data or decided by architecture
                    .addProperty("createdDate", versionTimestamp().toString())
                    .addProperty("validFrom", versionTimestamp().toString())
                    .addProperty("version", "1.0.0")
                    .addProperty("valuation", datasetMeta.getValuation().name())
                    .addProperty("versionValidFrom", versionTimestamp().toString())
                    .unitDataSet()
                    .temporalityType(DatasetTools.toTemporality("TODO")) // TODO: get this from correct place
                    .dataSetState(DatasetTools.toExplorationState(datasetMeta().getState()))
                    .dataSourcePath(datasetMeta().getId().getPath())
                    .build();

            // TODO we should always have this, even without dataset-doc. This could be based on e.g. avro schema
            ofNullable(unitDataStructure()).ifPresent(uds -> unitDataset.setUnitDataStructure(uds.link()));

            // must be assigned here before calling lineageDataset() which will recursively attempt to call this method
            datasetLdsObject = new LDSObject("UnitDataSet", datasetId(), versionTimestamp(), () -> unitDataset);

            ofNullable(lineageDataset()).ifPresent(ld -> unitDataset.setLineage(ld.link()));
        }

        return datasetLdsObject;
    }

    public List<LDSObject> logicalRecordsAndInstanceVariables() {
        if (logicalRecordsAndInstanceVariables == null) {
            logicalRecordsAndInstanceVariables = simpleToGsim().createLogicalRecordsAndInstanceVariables();
        }
        return logicalRecordsAndInstanceVariables;
    }

    private LineageTemplateToExplorationLineage toExplorationLineage() {
        if (toExplorationLineage == null) {
            JsonNode lineageDocNode = dataNode.get("dataset-lineage");
            if (lineageDocNode == null) {
                return null;
            }
            try {
                Dataset dataset = mapper.treeToValue(lineageDocNode, Dataset.class);
                toExplorationLineage = new LineageTemplateToExplorationLineage(dataset, unitDataSet());
            } catch (JsonProcessingException e) {
                throw new RuntimeException(e);
            }
        }
        return toExplorationLineage;
    }

    public LDSObject lineageDataset() {
        if (lineageDataset == null) {
            lineageDataset = ofNullable(toExplorationLineage())
                    .map(LineageTemplateToExplorationLineage::createLineageDatasetLdsObject)
                    .orElseGet(() -> {
                                String lineageDatasetId = DatasetTools.lineageDatasetId(
                                        DatasetTools.datasetId(datasetMeta.getId().getPath())
                                );
                                return new LDSObject("LineageDataset",
                                        lineageDatasetId,
                                        versionTimestamp(),
                                        () -> LineageDataset.newBuilder()
                                                .id(lineageDatasetId)
                                                .lineage(Collections.emptyList())
                                                .build());
                            }
                    );
        }
        return lineageDataset;
    }

    public List<LDSObject> lineageFields() {
        if (lineageFields == null) {
            JsonNode avroSchemaNode = dataNode.get("avro-schema");
            List<LDSObject> lineageFieldsFromAvroSchema;
            if (avroSchemaNode != null) {
                String avroSchemaJson;
                try {
                    avroSchemaJson = mapper.writeValueAsString(avroSchemaNode);
                } catch (JsonProcessingException e) {
                    throw new RuntimeException(e);
                }
                Schema avroSchema = new Schema.Parser().parse(avroSchemaJson);
                List<AvroSchemaField> fieldList = getAvroSchemaFields(avroSchema);
                lineageFieldsFromAvroSchema = transformAvroSchemaFieldsToLDSLineageFieldObjects(fieldList);
            } else {
                lineageFieldsFromAvroSchema = Collections.emptyList(); // backwards compatibility when avro-schema is missing
            }
            lineageFields = ofNullable(toExplorationLineage())
                    .map(eToE -> eToE.createLineageFieldLdsObjects(
                            logicalRecordsAndInstanceVariables().stream()
                                    .filter(o -> "InstanceVariable".equals(o.type))
                                    .collect(Collectors.toMap(LDSObject::id, o -> o)))
                    )
                    .map(lineageFieldsFromUser -> {
                        // avro-schema field ordering is preserved here
                        Map<String, LDSObject> avroLineageFieldByName = lineageFieldsFromAvroSchema.stream().collect(Collectors.toMap(o -> o.get(LineageField.class).getName(), o -> o, (a, b) -> b, LinkedHashMap::new));
                        Map<String, LDSObject> userLineageFieldByName = lineageFieldsFromUser.stream().collect(Collectors.toMap(o -> o.get(LineageField.class).getName(), o -> o, (a, b) -> b, LinkedHashMap::new));
                        List<LDSObject> result = new ArrayList<>();
                        avroLineageFieldByName.entrySet().stream()
                                .map(e -> userLineageFieldByName.containsKey(e.getKey())
                                        ? userLineageFieldByName.get(e.getKey()) // user lineage have precedence
                                        : e.getValue())
                                .forEach(result::add);
                        userLineageFieldByName.entrySet().stream()
                                .filter(e -> !avroLineageFieldByName.containsKey(e.getKey()))
                                .map(Map.Entry::getValue)
                                .forEach(result::add); // add all user fields that are not in avro-schema
                        return result;
                    })
                    .orElse(lineageFieldsFromAvroSchema);
        }
        return lineageFields;
    }

    private List<AvroSchemaField> getAvroSchemaFields(Schema avroSchema) {
        List<AvroSchemaField> fieldList = new ArrayList<>();
        AvroSchemaTraverser.dps(avroSchema, (ancestors, elem) -> {
            if (elem.getParentType().isEmpty()) {
                return; // root element
            }
            if (elem.isContainer()) {
                return;
            }
            if (Schema.Type.NULL.equals(elem.getSchema().getType())) {
                return; // null types are ignored as they only represent nullability of other types
            }
            String ancestorFieldName = StreamSupport.stream(
                    Spliterators.spliteratorUnknownSize(
                            ancestors.descendingIterator(),
                            Spliterator.ORDERED),
                    false)
                    .skip(1)
                    .filter(ctx -> ctx.name() != null)
                    .map(AvroSchemaTraverser.Context::name)
                    .collect(Collectors.joining("."));
            String qualifiedFieldName = of(ancestorFieldName)
                    .filter(a -> !a.isBlank())
                    .map(a -> ofNullable(elem.name())
                            .map(e -> String.join(".", a, e))
                            .orElse(a))
                    .orElseGet(elem::name);
            // System.out.printf("%s : %s%n", qualifiedFieldName, elem.schema.getType());
            fieldList.add(new AvroSchemaField(qualifiedFieldName, avroSchema.getType().name()));
        });
        return fieldList;
    }

    private List<LDSObject> transformAvroSchemaFieldsToLDSLineageFieldObjects(List<AvroSchemaField> fieldList) {
        return fieldList.stream()
                .map(avf -> {
                    String lineageFieldId = DatasetTools.lineageFieldId(lineageDataset().id(), avf.getQualifiedName());
                    return new LDSObject("LineageField",
                            lineageFieldId,
                            versionTimestamp(),
                            () -> LineageField.newBuilder()
                                    .id(lineageFieldId)
                                    .name(avf.getQualifiedName())
                                    .lineageDataset(lineageDataset().link())
                                    .relationType("unknown")
                                    .confidence(1.0) // 100% sure that the relation-type is unknown
                                    .build()
                    );
                })
                .collect(Collectors.toList());
    }
}
