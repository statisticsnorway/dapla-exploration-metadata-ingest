package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstanceVariable extends IdentifiableArtefact {

    @JsonProperty
    private String shortName;
    @JsonProperty
    private String dataStructureComponentType;
    @JsonProperty
    private String representedVariable;
    @JsonProperty
    private String sentinelValueDomain;
    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private String valuation;

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }


    public String getDataStructureComponentType() {
        return dataStructureComponentType;
    }

    public void setDataStructureComponentType(String dataStructureComponentType) {
        this.dataStructureComponentType = dataStructureComponentType;
    }

    public String getRepresentedVariable() {
        return representedVariable;
    }

    public void setRepresentedVariable(String representedVariable) {
        this.representedVariable = representedVariable;
    }

    public String getSentinelValueDomain() {
        return sentinelValueDomain;
    }

    public void setSentinelValueDomain(String sentinelValueDomain) {
        this.sentinelValueDomain = sentinelValueDomain;
    }

    public void setValuation(String valuation) {
        this.valuation = valuation;
    }
}
