package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class ProcessStepInstance extends IdentifiableArtefact {

    public static final String PROCESS_STEP_INSTANCE_NAME = "ProcessStepInstance";

    @JsonProperty
    private String processExecutionCode;

    @JsonProperty
    private String processExecutionLog;

    @JsonProperty
    private List<String> transformableInputs;

    @JsonProperty
    private List<String> transformedOutputs;

    public String getProcessExecutionCode() {
        return processExecutionCode;
    }

    public void setProcessExecutionCode(String processExecutionCode) {
        this.processExecutionCode = processExecutionCode;
    }

    public String getProcessExecutionLog() {
        return processExecutionLog;
    }

    public void setProcessExecutionLog(String processExecutionLog) {
        this.processExecutionLog = processExecutionLog;
    }

    public List<String> getTransformableInputs() {
        return transformableInputs;
    }

    public void setTransformableInputs(List<String> transformableInputs) {
        this.transformableInputs = transformableInputs;
    }

    public List<String> getTransformedOutputs() {
        return transformedOutputs;
    }

    public void setTransformedOutputs(List<String> transformedOutputs) {
        this.transformedOutputs = transformedOutputs;
    }
}
