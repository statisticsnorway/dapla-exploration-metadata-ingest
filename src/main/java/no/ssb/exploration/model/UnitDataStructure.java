package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UnitDataStructure extends IdentifiableArtefact {

    public static final String UNIT_DATA_STRUCTURE_NAME = "UnitDataStructure";
    @JsonProperty
    private List<String> logicalRecords;

    public List<String> getLogicalRecords() {
        return logicalRecords;
    }

    public void setLogicalRecords(List<String> logicalRecords) {
        this.logicalRecords = logicalRecords;
    }

    public void setShortName(String shortName) {
        setUnknowProperty("shortName", shortName);
    }
}
