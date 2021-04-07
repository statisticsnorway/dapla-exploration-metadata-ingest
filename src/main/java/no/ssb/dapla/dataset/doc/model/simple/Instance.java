package no.ssb.dapla.dataset.doc.model.simple;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

public class Instance {

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

    @JsonProperty
    private EnumInfo dataStructureComponentType;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private EnumInfo valuation;

    @JsonProperty
    private TypeInfo population;

    @JsonProperty
    private TypeInfo representedVariable;

    @JsonProperty
    private TypeInfo sentinelValueDomain;

    public String getName() {
        return name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TypeInfo getPopulation() {
        return population;
    }

    public EnumInfo getDataStructureComponentType() {
        return dataStructureComponentType;
    }

    public TypeInfo getRepresentedVariable() {
        return representedVariable;
    }

    public TypeInfo getSentinelValueDomain() {
        return sentinelValueDomain;
    }

    public EnumInfo getValuation() {
        return valuation;
    }
}
