package uk.gov.companieshouse.api.strikeoffobjections.model.eligibility;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ObjectionEligibility {

    @JsonProperty("is_eligible")
    private boolean isEligible;

    public boolean isEligible() {
        return isEligible;
    }

    public void setEligible(boolean eligible) {
        isEligible = eligible;
    }
}
