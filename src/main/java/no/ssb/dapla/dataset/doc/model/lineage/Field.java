package no.ssb.dapla.dataset.doc.model.lineage;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.ArrayList;
import java.util.List;

@JsonPropertyOrder({ "name", "type", "type_candidates", "confidence", "fields" })
public class Field {

    @JsonProperty
    protected String name;

    @JsonProperty
    protected String type;

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_NULL)
    protected Double confidence;

    public List<Source> getSources() {
        return sources;
    }

    @JsonProperty
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected List<Source> sources = new ArrayList<>();

    @JsonProperty("fields")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    protected final List<Field> fields = new ArrayList<>();

    public void setName(String name) {
        this.name = name;
    }

     public String getName() {
        return name;
    }

     public Double getConfidence() {
        return confidence;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public List<Field> getFields() {
        return fields;
    }
}
