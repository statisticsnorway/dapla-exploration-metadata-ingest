package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonAnySetter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

@JsonIgnoreProperties(ignoreUnknown = true)
public abstract class IdentifiableArtefact {

    @JsonProperty
    private String id;
    @JsonIgnore
    private Map<String, Object> unknownProperties = new LinkedHashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    @JsonAnyGetter
    public Map<String, Object> getUnknownProperties() {
        return unknownProperties;
    }

    @JsonAnySetter
    public void setUnknowProperty(String key, Object value) {
        unknownProperties.put(key, value);
    }

    @JsonIgnore
    public String getGsimName() {
        return this.getClass().getSimpleName();
    }

    @JsonIgnore
    public String getName() {
        // A bit of a hack to get name from gsim json structure. Should rewrite to have a class for this common structure
        Object name = unknownProperties.getOrDefault("name", null);
        if (name == null) return null;
        if (name instanceof List) {
            List<Map<String, String>> map = (List<Map<String, String>>) name;
            Optional<String> languageText = map.stream().map(a -> a.getOrDefault("languageText", null)).filter(Objects::nonNull).findFirst();
            if (languageText.isPresent()) return languageText.get();
        }
        return null;
    }
}
