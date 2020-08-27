package uk.gov.companieshouse.api.strikeoffobjections.validation;

import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

public class ValidationException extends Exception {

    private final ObjectionStatus status;

    public ValidationException(ObjectionStatus status) {
        this.status = status;
    }

    public ObjectionStatus getStatus() {
        return status;
    }
}
