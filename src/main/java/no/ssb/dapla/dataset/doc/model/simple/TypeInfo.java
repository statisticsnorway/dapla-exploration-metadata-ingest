package no.ssb.dapla.dataset.doc.model.simple;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

public class TypeInfo {
    @JsonProperty("selected-id")
    private String id;

    @JsonProperty("concept-type")
    private String type;

    @JsonProperty("candidates")
    private List<Candidate> candidates;

    public String getId() {
        return id;
    }

    public String getType() {
        return type;
    }
}
