package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class InstanceVariable extends IdentifiableArtefact {

    public static final String INSTANCE_VARIABLE_NAME = "InstanceVariable";

    @JsonProperty
    private String shortName;
    @JsonProperty
    private String dataStructureComponentRole;
    @JsonProperty
    private String dataStructureComponentType;
    @JsonProperty
    private boolean identifierComponentIsComposite;
    @JsonProperty
    private boolean identifierComponentIsUnique;
    @JsonProperty
    private String representedVariable;
    @JsonProperty
    private String sentinelValueDomain;

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getDataStructureComponentRole() {
        return dataStructureComponentRole;
    }

    public void setDataStructureComponentRole(String dataStructureComponentRole) {
        this.dataStructureComponentRole = dataStructureComponentRole;
    }

    public String getDataStructureComponentType() {
        return dataStructureComponentType;
    }

    public void setDataStructureComponentType(String dataStructureComponentType) {
        this.dataStructureComponentType = dataStructureComponentType;
    }

    public boolean getIdentifierComponentIsComposite() {
        return identifierComponentIsComposite;
    }

    public void setIdentifierComponentIsComposite(boolean identifierComponentIsComposite) {
        this.identifierComponentIsComposite = identifierComponentIsComposite;
    }

    public boolean getIdentifierComponentIsUnique() {
        return identifierComponentIsUnique;
    }

    public void setIdentifierComponentIsUnique(boolean identifierComponentIsUnique) {
        this.identifierComponentIsUnique = identifierComponentIsUnique;
    }

    public String getRepresentedVariable() {
        return representedVariable;
    }

    public void setRepresentedVariable(String representedVariable) {
        this.representedVariable = representedVariable;
    }

    public String getSentinelValueDomain() {
        return sentinelValueDomain;
    }

    public void setSentinelValueDomain(String sentinelValueDomain) {
        this.sentinelValueDomain = sentinelValueDomain;
    }

}
