package no.ssb.dapla.dataset.doc.model.lineage;

import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import no.ssb.dapla.dataset.doc.model.simple.Instance;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class Field {
    private static final Logger LOG = LoggerFactory.getLogger(Instance.class);

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

    @JsonAnySetter
    public void setUnknowProperty(String key, Object value) {
        LOG.warn("Create Instance from json, key:{} with value:{} will be ignored!", key, value);
    }
}
