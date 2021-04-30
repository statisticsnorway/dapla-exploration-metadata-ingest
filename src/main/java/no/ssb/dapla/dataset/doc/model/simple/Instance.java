package no.ssb.dapla.dataset.doc.model.simple;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.ssb.dapla.exploration_metadata_ingest.DatasetUpstreamGooglePubSubIntegration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Instance {
    private static final Logger LOG = LoggerFactory.getLogger(Instance.class);

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

    @JsonAnySetter
    public void setUnknowProperty(String key, Object value) {
        LOG.warn("Create Instance from json, key:{} with value:{} will be ignored!", key, value);
    }

}
