package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TransformableInput extends IdentifiableArtefact {

    public static final String TRANSFORMABLE_INPUT_NAME = "TransformableInput";

    @JsonProperty
    private String inputId;

    public String getInputId() {
        return inputId;
    }

    public void setInputId(String inputId) {
        this.inputId = inputId;
    }
}
