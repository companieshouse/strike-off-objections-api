package uk.gov.companieshouse.api.strikeoffobjections.validation;

import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

import java.util.List;

public class AllowedValuesValidationRule implements ValidationRule {

    private List<Object> allowableValues;
    private ObjectionStatus failureStatus;

    public AllowedValuesValidationRule(List<Object> allowableValues, ObjectionStatus failureStatus) {
        this.allowableValues = allowableValues;
        this.failureStatus = failureStatus;
    }

    @Override
    public void validate(Object input) throws ValidationException {
       // TODO logging
        if (!allowableValues.contains(input)) {
            throw new ValidationException(failureStatus);
        }
    }

}
