package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.util.List;
import java.util.Map;

@JsonIgnoreProperties(ignoreUnknown = true)
public class StatisticalProgram extends IdentifiableArtefact {

    public static final String STATISTICAL_PROGRAM_NAME = "StatisticalProgram";

    @JsonProperty
    private String statisticalProgramStatus;

    @JsonProperty
    private List<String> statisticalProgramCycles;

    @JsonProperty
    private List<Map<String, Object>> subjectMatterDomains;

    public void setStatisticalProgramStatus(String statisticalProgramStatus) {
        this.statisticalProgramStatus = statisticalProgramStatus;
    }

    public void setStatisticalProgramCycles(List<String> statisticalProgramCycles) {
        this.statisticalProgramCycles = statisticalProgramCycles;
    }

    public void setSubjectMatterDomains(List<Map<String, Object>> subjectMatterDomains) {
        this.subjectMatterDomains = subjectMatterDomains;
    }
}
