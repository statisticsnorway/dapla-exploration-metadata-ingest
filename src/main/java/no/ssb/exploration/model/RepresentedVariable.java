package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class RepresentedVariable extends IdentifiableArtefact {

    private static final String REPRESENTED_VARIABLE_NAME = "RepresentedVariable";

    @JsonProperty
    private String substantiveValueDomain;

    public String getSubstantiveValueDomain() {
        return substantiveValueDomain;
    }

    public void setSubstantiveValueDomain(String substantiveValueDomain) {
        this.substantiveValueDomain = substantiveValueDomain;
    }

}
