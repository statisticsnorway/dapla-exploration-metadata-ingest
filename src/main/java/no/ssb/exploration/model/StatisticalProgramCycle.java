package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatisticalProgramCycle extends IdentifiableArtefact {

    public static final String STATISTICAL_PROGRAM_CYCLE_NAME = "StatisticalProgramCycle";

    @JsonProperty
    private String referencePeriodEnd;

    @JsonProperty
    private String referencePeriodStart;

    public List<String> getBusinessProcesses() {
        return businessProcesses;
    }

    @JsonProperty
    private List<String> businessProcesses;

    public void setReferencePeriodEnd(String referencePeriodEnd) {
        this.referencePeriodEnd = referencePeriodEnd;
    }

    public void setReferencePeriodStart(String referencePeriodStart) {
        this.referencePeriodStart = referencePeriodStart;
    }

    public void setBusinessProcesses(List<String> businessProcesses) {
        this.businessProcesses = businessProcesses;
    }
}
