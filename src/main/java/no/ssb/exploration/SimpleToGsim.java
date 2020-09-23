package no.ssb.exploration;

import no.ssb.dapla.dataset.doc.model.simple.Instance;
import no.ssb.dapla.dataset.doc.model.simple.Record;
import no.ssb.exploration.model.InstanceVariable;
import no.ssb.exploration.model.LogicalRecord;
import no.ssb.exploration.model.PersistenceProvider;
import no.ssb.exploration.model.UnitDataStructure;

public class SimpleToGsim {
    private final String dataSetPath;
    private final PersistenceProvider persistenceProvider;
    private String userName = "Unknown";
    private final Record rootRecord;

    public GsimBuilder.BaseBuilder createDefault(String id, String name, String description) {
        // for now just add hardcoded default values
        String date = "2020-01-01T00:00:00Z";
        return GsimBuilder.create()
                .id(id)
                .languageCode("nb")
                .name(name)
                .description(description)
                .createdBy(userName)
                .addProperty("administrativeStatus", "DRAFT")
                .addProperty("createdDate", date)
                .addProperty("validFrom", date)
                .addProperty("version", "1.0.0")
                .addProperty("versionValidFrom", date);
    }

    public SimpleToGsim(Record rootRecord, String dataSetPath, PersistenceProvider persistenceProvider) {
        if (!dataSetPath.startsWith("/")) {
            throw new IllegalArgumentException("dataset path is expected to start with: '/' but was: " + dataSetPath);
        }
        this.rootRecord = rootRecord;
        this.dataSetPath = dataSetPath;
        this.persistenceProvider = persistenceProvider;
    }

    public SimpleToGsim createdBy(String userName) {
        this.userName = userName;
        return this;
    }

    public void createGsimObjects() {
        Record rootRecord = this.rootRecord;
        processAll(rootRecord, null);
    }

    public UnitDataStructure createUnitDataStructure(Record rootRecord) {
        return createDefault(createId(rootRecord), rootRecord.getName(), rootRecord.getDescription())
                .unitDataStructure()
                .logicalRecord(createId(rootRecord))
                .build();
    }

    void processAll(Record record, String parentLogicalRecordId) {
        String logicalRecordId = parentLogicalRecordId == null ? createId(record) : parentLogicalRecordId + "." + record.getName();
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

        persistenceProvider.save(gsimLogicalRecord);

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

            persistenceProvider.save(gsimInstanceVariable);
        }

        for (Record child : record.getRecords()) {
            processAll(child, logicalRecordId);
        }
    }

    private String createId(Record record) {
        String path = this.dataSetPath.substring(1); // Remove first slash
        return path.replace("/", ".") + "." + record.getName();
    }

    private String createId(Record record, Instance instance) {
        return createId(record) + "." + instance.getName();
    }
}
