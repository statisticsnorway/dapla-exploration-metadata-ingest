package no.ssb.dapla.dataset.doc.model.lineage;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

public class Source {

    @JsonProperty("field_candidates")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private List<String> fieldCandidates;

    @JsonProperty
    private String field;

    @JsonProperty
    private String path;

    @JsonProperty
    private long version;

    @JsonIgnore
    private double confidence;

    @JsonIgnore
    private double matchScore;

    public long getVersion() {
        return version;
    }

    public String getField() {
        return field;
    }

    public String getPath() {
        return path;
    }
}
