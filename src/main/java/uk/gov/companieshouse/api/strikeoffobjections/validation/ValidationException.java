package uk.gov.companieshouse.api.strikeoffobjections.validation;

import uk.gov.companieshouse.api.strikeoffobjections.model.eligibility.EligibilityStatus;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

public class ValidationException extends Exception {

    private final EligibilityStatus eligibilityStatus;

    public ValidationException(EligibilityStatus eligibilityStatus) {
        this.eligibilityStatus = eligibilityStatus;
    }

    public EligibilityStatus getEligibilityStatus() {
        return eligibilityStatus;
    }

    public ObjectionStatus getObjectionStatus() {
        return eligibilityStatus.getObjectionStatus();
    }
}
