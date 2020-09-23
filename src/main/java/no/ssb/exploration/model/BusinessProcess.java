package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class BusinessProcess extends IdentifiableArtefact {

    public static final String BUSINESS_PROCESS_NAME = "BusinessProcess";

    @JsonProperty
    private List<String> processSteps;

    @JsonProperty
    private boolean isPlaceholderProcess;

    public boolean hasPlaceholderProcess() {
        return this.isPlaceholderProcess;
    }

    public void setPlaceholderProcess(boolean placeholderProcess) {
        this.isPlaceholderProcess = placeholderProcess;
    }

    public List<String> getProcessSteps() {
        return processSteps;
    }

    public void setProcessSteps(List<String> processSteps) {
        this.processSteps = processSteps;
    }

}
