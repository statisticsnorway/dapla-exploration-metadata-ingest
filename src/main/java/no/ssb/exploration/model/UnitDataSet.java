package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class UnitDataSet extends Dataset {

    public static final String UNIT_DATA_SET_NAME = "UnitDataSet";
    @JsonProperty
    private String unitDataStructure;

    public String getUnitDataStructure() {
        return unitDataStructure;
    }

    public void setUnitDataStructure(String unitDataStructure) {
        this.unitDataStructure = unitDataStructure;
    }
}
