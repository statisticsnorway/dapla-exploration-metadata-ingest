package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class LineageDataset implements LineageObject {

    @JsonProperty
    protected String id;

    @JsonProperty
    private List<String> lineage;

    @Override
    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public List<String> getLineage() {
        return lineage;
    }

    public void setLineage(List<String> lineage) {
        this.lineage = lineage;
    }

    public static Builder newBuilder() {
        return new Builder();
    }

    public static class Builder {
        final LineageDataset instance = new LineageDataset();

        private Builder() {
        }

        public Builder id(String id) {
            instance.id = id;
            return this;
        }

        public Builder lineage(List<String> lineage) {
            instance.lineage = lineage;
            return this;
        }

        public LineageDataset build() {
            return instance;
        }
    }
}
