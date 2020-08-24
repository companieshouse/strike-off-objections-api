package uk.gov.companieshouse.api.strikeoffobjections.validation;

import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

import java.util.List;

public class DisallowedValuesValidationRule implements ValidationRule {

    private List<Object> disallowedValues;
    private ObjectionStatus failureStatus;

    public DisallowedValuesValidationRule(List<Object> disallowedValues, ObjectionStatus failureStatus) {
        this.disallowedValues = disallowedValues;
        this.failureStatus = failureStatus;
    }

    @Override
    public void validate(Object input) throws ValidationException {
        // TODO logging
        if (disallowedValues.contains(input)) {
            throw new ValidationException(failureStatus);
        }
    }
}
