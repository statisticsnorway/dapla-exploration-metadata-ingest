package no.ssb.dapla.dataset.doc.model.simple;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyOrder;

import java.util.List;

public class EnumInfo {
    @JsonProperty("selected-enum")
    private String value;

    @JsonProperty("enums")
    private List<String> enums;

    @JsonProperty("optional")
    @JsonInclude(JsonInclude.Include.NON_EMPTY)
    private Boolean optional;

    public String getValue() {
        return value;
    }

}
