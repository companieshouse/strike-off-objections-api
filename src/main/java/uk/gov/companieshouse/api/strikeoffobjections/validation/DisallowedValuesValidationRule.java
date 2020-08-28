package uk.gov.companieshouse.api.strikeoffobjections.validation;

import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

import java.util.List;

public class DisallowedValuesValidationRule<T> implements ValidationRule<T> {

    private List<T> disallowedValues;
    private ObjectionStatus failureStatus;
    private ApiLogger apiLogger;

    public DisallowedValuesValidationRule(List<T> disallowedValues,
                                          ObjectionStatus failureStatus,
                                          ApiLogger apiLogger) {
        this.disallowedValues = disallowedValues;
        this.failureStatus = failureStatus;
        this.apiLogger = apiLogger;
    }

    @Override
    public void validate(T input, String logContext) throws ValidationException {
        if (disallowedValues.contains(input)) {
            apiLogger.debugContext(logContext, String.format("DisallowedValuesValidationRule %s is a disallowed value", input));
            throw new ValidationException(failureStatus);
        }
    }
}
