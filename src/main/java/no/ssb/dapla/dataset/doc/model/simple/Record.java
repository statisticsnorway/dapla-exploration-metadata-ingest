package no.ssb.dapla.dataset.doc.model.simple;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Record {
    public interface CreateIdHandler {

        String createId(Instance name);
    }
    @JsonProperty
    private String shortName;

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    private TypeInfo unitType;

    @JsonProperty("instanceVariables")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<Instance> instances = new ArrayList<>();

    @JsonProperty("logicalRecords")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private final List<Record> records = new ArrayList<>();

    public String getName() {
        return name;
    }

    public String getShortName() {
        if (shortName != null) return shortName;
        // To make it work for older versions
        return name;
    }

    public String getDescription() {
        return description == null ? "" : description;
    }

    public TypeInfo getUnitType() {
        return unitType;
    }

    public List<Record> getRecords() {
        return records;
    }

    public List<Instance> getInstances() {
        return instances;
    }

    public List<String> getInstanceVariableIds(CreateIdHandler createIdHandler) {
        return instances.stream().map(i -> "/" + createIdHandler.createId(i)).collect(Collectors.toList());
    }
}
