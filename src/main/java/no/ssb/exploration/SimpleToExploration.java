package no.ssb.exploration;

import no.ssb.dapla.dataset.doc.model.simple.Instance;
import no.ssb.dapla.dataset.doc.model.simple.Record;
import no.ssb.exploration.model.GsimBuilder;
import no.ssb.exploration.model.InstanceVariable;
import no.ssb.exploration.model.LogicalRecord;
import no.ssb.exploration.model.UnitDataStructure;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Spliterator;
import java.util.Spliterators;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static java.util.Optional.of;
import static java.util.Optional.ofNullable;

public class SimpleToExploration {
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

    public SimpleToExploration(Record rootRecord, String dataSetPath, ZonedDateTime version) {
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

    public SimpleToExploration createdBy(String userName) {
        this.userName = userName;
        return this;
    }

    public List<LDSObject> createLogicalRecordsAndInstanceVariables() {
        if (rootRecord == null) {
            return Collections.emptyList();
        }
        List<LDSObject> result = new LinkedList<>();
        Deque<Record> ancestors = new LinkedList<>();
        processAll(result, ancestors, rootRecord, null);
        return result;
    }

    public UnitDataStructure createUnitDataStructure() {
        String recordName = ofNullable(rootRecord).map(Record::getName).orElse("");
        String description = ofNullable(rootRecord).map(Record::getDescription).orElse("");
        UnitDataStructure unitDataStructure = createDefault(DatasetTools.datasetId(dataSetPath), recordName, description)
                .unitDataStructure()
                .build();
        List<String> logicalRecords = new ArrayList<>();
        if (rootRecord != null) {
            logicalRecords.add("/LogicalRecord/" + logialRecordId(recordName));
        }
        unitDataStructure.setLogicalRecords(logicalRecords);
        return unitDataStructure;
    }

    void processAll(List<LDSObject> result, Deque<Record> ancestors, Record record, String parentLogicalRecordId) {
        ancestors.push(record);
        String ancestorFieldName = StreamSupport.stream(
                Spliterators.spliteratorUnknownSize(
                        ancestors.descendingIterator(),
                        Spliterator.ORDERED),
                false)
                .skip(1) // skip root record
                .map(Record::getName)
                .collect(Collectors.joining("."));
        String logicalRecordId = parentLogicalRecordId == null ? logialRecordId(record.getName()) : parentLogicalRecordId + "." + record.getName();
        LogicalRecord gsimLogicalRecord =
                createDefault(logicalRecordId, record.getName(), record.getDescription())
                        .logicalRecord()
                        .isPlaceholderRecord(false)// TODO: add and get from simple
                        .unitType(record.getUnitType(), "UnitType_DUMMY")
                        .shortName(record.getName())
                        .instanceVariables(record.getInstanceVariableIds(i -> {
                            String id = instanceVariableId(of(ancestorFieldName)
                                    .filter(a -> !a.isBlank())
                                    .map(a -> ofNullable(i.getName())
                                            .map(e -> String.join(".", a, e))
                                            .orElse(a))
                                    .orElseGet(i::getName));
                            return id;
                        }))
                        .parent(parentLogicalRecordId)
                        .parentChildMultiplicity("ONE_MANY")
                        .build();

        result.add(new LDSObject("LogicalRecord", gsimLogicalRecord.getId(), version, () -> gsimLogicalRecord));

        for (Instance instance : record.getInstances()) {
            String id = instanceVariableId(of(ancestorFieldName)
                    .filter(a -> !a.isBlank())
                    .map(a -> ofNullable(instance.getName())
                            .map(e -> String.join(".", a, e))
                            .orElse(a))
                    .orElseGet(instance::getName));
            InstanceVariable gsimInstanceVariable =
                    createDefault(id, instance.getName(), instance.getDescription())
                            .instanceVariable()
                            .shortName(instance.getName())
                            .population(instance.getPopulation(), "Population_DUMMY")
                            .dataStructureComponentType(instance.getDataStructureComponentType(), "MEASURE")
                            .valuation(instance.getValuation())
                            .sentinelValueDomain(instance.getSentinelValueDomain(), "DescribedValueDomain_DUMMY")
                            .representedVariable(instance.getRepresentedVariable(), "RepresentedVariable_DUMMY")
                            .build();

            result.add(new LDSObject("InstanceVariable", gsimInstanceVariable.getId(), version, () -> gsimInstanceVariable));
        }

        for (Record child : record.getRecords()) {
            processAll(result, ancestors, child, logicalRecordId);
        }
        ancestors.pop();
    }

    public String logialRecordId(String recordName) {
        String id = DatasetTools.logicalRecordId(DatasetTools.datasetId(dataSetPath), recordName);
        return id;
    }

    private String instanceVariableId(String qualifiedFieldName) {
        String id = DatasetTools.instanceVariableId(DatasetTools.lineageDatasetId(DatasetTools.datasetId(dataSetPath)), qualifiedFieldName);
        return id;
    }
}
