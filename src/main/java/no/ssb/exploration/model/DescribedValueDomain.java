package no.ssb.exploration.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DescribedValueDomain extends ValueDomain {

    static final String DESCRIBED_VALUE_DOMAIN_NAME = "DescribedValueDomain";

}
