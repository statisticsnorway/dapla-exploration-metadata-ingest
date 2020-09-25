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
    private Double confidence;

    @JsonProperty
    private String instanceVariable;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getLineageDataset() {
        return lineageDataset;
    }

    public void setLineageDataset(String lineageDataset) {
        this.lineageDataset = lineageDataset;
    }

    public String getRelationType() {
        return relationType;
    }

    public void setRelationType(String relationType) {
        this.relationType = relationType;
    }

    public List<String> getLineage() {
        return lineage;
    }

    public void setLineage(List<String> lineage) {
        this.lineage = lineage;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }

    public String getInstanceVariable() {
        return instanceVariable;
    }

    public void setInstanceVariable(String instanceVariable) {
        this.instanceVariable = instanceVariable;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        final LineageField instance = new LineageField();

        private Builder() {
        }

        public Builder id(String id) {
            instance.id = id;
            return this;
        }

        public Builder name(String name) {
            instance.name = name;
            return this;
        }

        public Builder lineageDataset(String lineageDataset) {
            instance.lineageDataset = lineageDataset;
            return this;
        }

        public Builder relationType(String relationType) {
            instance.relationType = relationType;
            return this;
        }

        public Builder lineage(List<String> lineage) {
            instance.lineage = lineage;
            return this;
        }

        public Builder confidence(Double confidence) {
            instance.confidence = confidence;
            return this;
        }

        public Builder instanceVariable(String instanceVariable) {
            instance.instanceVariable = instanceVariable;
            return this;
        }

        public LineageField build() {
            return instance;
        }
    }
}
