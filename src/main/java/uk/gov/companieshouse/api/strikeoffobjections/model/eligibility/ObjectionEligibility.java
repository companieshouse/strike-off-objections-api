package uk.gov.companieshouse.api.strikeoffobjections.model.eligibility;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ObjectionEligibility {

    @JsonProperty("is_eligible")
    private boolean isEligible;

    public ObjectionEligibility(boolean isEligible) {
        this.isEligible = isEligible;
    }

    public boolean isEligible() {
        return isEligible;
    }
}
