package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessStep extends IdentifiableArtefact {

    public static final String PROCESS_STEP_NAME = "ProcessStep";

    @JsonProperty
    private List<Map> codeBlocks;

    public List<Map> getCodeBlocks() {
        return codeBlocks;
    }

    public void setCodeBlocks(List<Map> codeBlocks) {
        this.codeBlocks = codeBlocks;
    }
}
