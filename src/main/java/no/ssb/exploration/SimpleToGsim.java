package no.ssb.exploration;

import no.ssb.dapla.dataset.doc.model.simple.Instance;
import no.ssb.dapla.dataset.doc.model.simple.Record;
import no.ssb.exploration.model.InstanceVariable;
import no.ssb.exploration.model.LDSObject;
import no.ssb.exploration.model.LogicalRecord;
import no.ssb.exploration.model.UnitDataStructure;

import java.time.ZonedDateTime;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class SimpleToGsim {
    private final String dataSetPath;
    private final ZonedDateTime version;
    private String userName = "Unknown";
    private final Record rootRecord;

    public GsimBuilder.BaseBuilder createDefault(String id, String name, String description) {
        // for now just add hardcoded default values
        return GsimBuilder.create()
                .id(id)
                .languageCode("nb")
                .name(name)
                .description(description)
                .createdBy(userName)
                .addProperty("administrativeStatus", "DRAFT")
                .addProperty("createdDate", versionAsString())
                .addProperty("validFrom", versionAsString())
                .addProperty("version", "1.0.0")
                .addProperty("versionValidFrom", versionAsString());
    }

    public SimpleToGsim(Record rootRecord, String dataSetPath, ZonedDateTime version) {
        this.version = version;
        if (!dataSetPath.startsWith("/")) {
            throw new IllegalArgumentException("dataset path is expected to start with: '/' but was: " + dataSetPath);
        }
        this.rootRecord = rootRecord;
        this.dataSetPath = dataSetPath;
    }

    private String versionAsString() {
        return version.toString();
    }

    public SimpleToGsim createdBy(String userName) {
        this.userName = userName;
        return this;
    }

    public void createGsimObjects(Map<String, List<LDSObject>> output) {
        Record rootRecord = this.rootRecord;
        processAll(output, rootRecord, null);
    }

    public UnitDataStructure createUnitDataStructure(Record rootRecord) {
        return createDefault(createId(dataSetPath, rootRecord), rootRecord.getName(), rootRecord.getDescription())
                .unitDataStructure()
                .logicalRecord(createId(dataSetPath, rootRecord))
                .build();
    }

    void processAll(Map<String, List<LDSObject>> output, Record record, String parentLogicalRecordId) {
        String logicalRecordId = parentLogicalRecordId == null ? createId(dataSetPath, record) : parentLogicalRecordId + "." + record.getName();
        LogicalRecord gsimLogicalRecord =
                createDefault(logicalRecordId, record.getName(), record.getDescription())
                        .logicalRecord()
                        .isPlaceholderRecord(false)// TODO: add and get from simple
                        .unitType(record.getUnitType(), "UnitType_DUMMY")
                        .shortName(record.getName())
                        .instanceVariables(record.getInstanceVariableIds(i -> createId(record, i)))
                        .parent(parentLogicalRecordId)
                        .parentChildMultiplicity("ONE_MANY")
                        .build();

        output.computeIfAbsent("LogicalRecord", k -> new LinkedList<>())
                .add(new LDSObject("LogicalRecord", gsimLogicalRecord.getId(), version, () -> gsimLogicalRecord));

        for (Instance instance : record.getInstances()) {
            InstanceVariable gsimInstanceVariable =
                    createDefault(createId(record, instance), instance.getName(), instance.getDescription())
                            .instanceVariable()
                            .shortName(instance.getName())
                            .population(instance.getPopulation(), "Population_DUMMY")
                            .dataStructureComponentType(instance.getDataStructureComponentType(), "MEASURE")
                            .dataStructureComponentRole(instance.getDataStructureComponentRole(), "ENTITY")
                            .identifierComponentIsComposite(instance.getIdentifierComponentIsComposite())
                            .identifierComponentIsUnique(instance.getIdentifierComponentIsUnique())
                            .sentinelValueDomain(instance.getSentinelValueDomain(), "DescribedValueDomain_DUMMY")
                            .representedVariable(instance.getRepresentedVariable(), "RepresentedVariable_DUMMY")
                            .build();

            output.computeIfAbsent("InstanceVariable", k -> new LinkedList<>())
                    .add(new LDSObject("InstanceVariable", gsimInstanceVariable.getId(), version, () -> gsimInstanceVariable));
        }

        for (Record child : record.getRecords()) {
            processAll(output, child, logicalRecordId);
        }
    }

    public static String createId(String dataSetPath, Record record) {
        String path = dataSetPath.substring(1); // Remove first slash
        return path.replace("/", ".") + "." + record.getName();
    }

    private String createId(Record record, Instance instance) {
        return createId(dataSetPath, record) + "." + instance.getName();
    }
}
