package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class EnumeratedValueDomain extends ValueDomain {

    static final String ENUMERATED_VALUE_NAME = "EnumeratedValueDomain";

    @JsonProperty
    private String klassUrl;

    public String getKlassUrl() {
        return klassUrl;
    }

    public void setKlassUrl(String klassUrl) {
        this.klassUrl = klassUrl;
    }
}
