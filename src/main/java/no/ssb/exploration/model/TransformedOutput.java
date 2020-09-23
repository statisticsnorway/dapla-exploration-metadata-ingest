package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransformedOutput extends IdentifiableArtefact {

    public static final String TRANSFORMABLE_OUTPUT_NAME = "TransformedOutput";

    @JsonProperty
    private String outputId;

    public void setOutputId(String outputId) {
        this.outputId = outputId;
    }
}
