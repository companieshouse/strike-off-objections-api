package uk.gov.companieshouse.api.strikeoffobjections.validation;

import java.util.List;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.model.eligibility.EligibilityStatus;

public class DisallowedValuesValidationRule<T> implements ValidationRule<T> {

    private List<T> disallowedValues;
    private EligibilityStatus failureStatus;
    private ApiLogger apiLogger;

    public DisallowedValuesValidationRule(
            List<T> disallowedValues, EligibilityStatus failureStatus, ApiLogger apiLogger) {
        this.disallowedValues = disallowedValues;
        this.failureStatus = failureStatus;
        this.apiLogger = apiLogger;
    }

    @Override
    public void validate(T input, String logContext) throws ValidationException {
        if (disallowedValues.contains(input)) {
            apiLogger.debugContext(
                    logContext,
                    String.format("%s %s is a disallowed value", this.getClass().getSimpleName(), input));
            throw new ValidationException(failureStatus);
        }
    }
}
