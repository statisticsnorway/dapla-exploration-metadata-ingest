package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LineageField implements LineageObject {

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String lineageDataset;

    @JsonProperty
    private String relationType;

    @JsonProperty
    private List<String> lineage;

    @JsonProperty
    private float confidence;

    @JsonProperty
    private String instanceVariable;

    @Override
    public String getId() {
        return id;
    }
}
