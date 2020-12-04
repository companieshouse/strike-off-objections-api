package uk.gov.companieshouse.api.strikeoffobjections.model.eligibility;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ObjectionEligibility {

    @JsonProperty("is_eligible")
    private final boolean eligible;

    @JsonProperty("eligibility_status")
    private final EligibilityStatus eligibilityStatus;

    public ObjectionEligibility(boolean eligible, EligibilityStatus eligibilityStatus) {
        this.eligible = eligible;
        this.eligibilityStatus = eligibilityStatus;
    }

    public boolean isEligible() {
        return eligible;
    }

    public EligibilityStatus getEligibilityStatus() {
        return eligibilityStatus;
    }
}
