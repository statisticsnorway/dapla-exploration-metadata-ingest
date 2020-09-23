package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LogicalRecord extends IdentifiableArtefact {

    @JsonProperty
    private String shortName;

    @JsonProperty
    private String unitType;

    @JsonProperty
    private List<String> instanceVariables;

    public String getUnitType() {
        return unitType;
    }

    public void setUnitType(String unitType) {
        this.unitType = unitType;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public List<String> getInstanceVariables() {
        return instanceVariables;
    }

    public void setInstanceVariables(List<String> instanceVariables) {
        this.instanceVariables = instanceVariables;
    }
}
