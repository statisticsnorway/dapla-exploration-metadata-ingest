package no.ssb.exploration;

import no.ssb.dapla.dataset.doc.model.simple.Instance;
import no.ssb.dapla.dataset.doc.model.simple.Record;
import no.ssb.exploration.model.GsimBuilder;
import no.ssb.exploration.model.InstanceVariable;
import no.ssb.exploration.model.LogicalRecord;
import no.ssb.exploration.model.UnitDataStructure;

import java.time.ZonedDateTime;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

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

    public List<LDSObject> createLogicalRecordsAndInstanceVariables() {
        if (rootRecord == null) {
            return Collections.emptyList();
        }
        List<LDSObject> result = new LinkedList<>();
        processAll(result, rootRecord, null);
        return result;
    }

    public UnitDataStructure createUnitDataStructure() {
        if (rootRecord == null) {
            return null;
        }
        return createDefault(logialRecordId(rootRecord), rootRecord.getName(), rootRecord.getDescription())
                .unitDataStructure()
                .logicalRecord(logialRecordId(rootRecord))
                .build();
    }

    void processAll(List<LDSObject> result, Record record, String parentLogicalRecordId) {
        String logicalRecordId = parentLogicalRecordId == null ? logialRecordId(record) : parentLogicalRecordId + "." + record.getName();
        LogicalRecord gsimLogicalRecord =
                createDefault(logicalRecordId, record.getName(), record.getDescription())
                        .logicalRecord()
                        .isPlaceholderRecord(false)// TODO: add and get from simple
                        .unitType(record.getUnitType(), "UnitType_DUMMY")
                        .shortName(record.getName())
                        .instanceVariables(record.getInstanceVariableIds(i -> instanceVariableId(record, i)))
                        .parent(parentLogicalRecordId)
                        .parentChildMultiplicity("ONE_MANY")
                        .build();

        result.add(new LDSObject("LogicalRecord", gsimLogicalRecord.getId(), version, () -> gsimLogicalRecord));

        for (Instance instance : record.getInstances()) {
            InstanceVariable gsimInstanceVariable =
                    createDefault(instanceVariableId(record, instance), instance.getName(), instance.getDescription())
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

            result.add(new LDSObject("InstanceVariable", gsimInstanceVariable.getId(), version, () -> gsimInstanceVariable));
        }

        for (Record child : record.getRecords()) {
            processAll(result, child, logicalRecordId);
        }
    }

    public String logialRecordId(Record record) {
        String id = DatasetTools.logialRecordId(DatasetTools.datasetId(dataSetPath), record.getName());
        return id;
    }

    private String instanceVariableId(Record record, Instance instance) {
        String id = DatasetTools.instanceVariableId(DatasetTools.logialRecordId(DatasetTools.datasetId(dataSetPath), record.getName()), instance.getName());
        return id;
    }
}
