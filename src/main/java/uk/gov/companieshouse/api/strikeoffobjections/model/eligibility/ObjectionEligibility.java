package uk.gov.companieshouse.api.strikeoffobjections.model.eligibility;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ObjectionEligibility {

    @JsonProperty("is_eligible")
    private boolean eligible;

    public ObjectionEligibility(boolean eligible) {
        this.eligible = eligible;
    }

    public boolean isEligible() {
        return eligible;
    }
}
