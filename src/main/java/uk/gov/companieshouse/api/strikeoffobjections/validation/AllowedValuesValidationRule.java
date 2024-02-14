package uk.gov.companieshouse.api.strikeoffobjections.validation;

import java.util.List;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.model.eligibility.EligibilityStatus;

public class AllowedValuesValidationRule<T> implements ValidationRule<T> {

    private List<T> allowableValues;
    private EligibilityStatus failureStatus;
    private ApiLogger apiLogger;

    public AllowedValuesValidationRule(
            List<T> allowableValues, EligibilityStatus failureStatus, ApiLogger apiLogger) {
        this.allowableValues = allowableValues;
        this.failureStatus = failureStatus;
        this.apiLogger = apiLogger;
    }

    @Override
    public void validate(T input, String logContext) throws ValidationException {
        if (!allowableValues.contains(input)) {
            apiLogger.debugContext(
                    logContext,
                    String.format("%s %s is not an allowed value", this.getClass().getSimpleName(), input));
            throw new ValidationException(failureStatus);
        }
    }
}
