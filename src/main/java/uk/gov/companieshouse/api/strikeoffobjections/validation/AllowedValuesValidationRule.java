package uk.gov.companieshouse.api.strikeoffobjections.validation;

import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

import java.util.List;

public class AllowedValuesValidationRule<T> implements ValidationRule<T> {

    private List<T> allowableValues;
    private ObjectionStatus failureStatus;
    private ApiLogger apiLogger;

    public AllowedValuesValidationRule(List<T> allowableValues,
                                       ObjectionStatus failureStatus,
                                       ApiLogger apiLogger) {
        this.allowableValues = allowableValues;
        this.failureStatus = failureStatus;
        this.apiLogger = apiLogger;
    }

    @Override
    public void validate(T input, String logContext) throws ValidationException {
        if (!allowableValues.contains(input)) {
            apiLogger.debugContext(logContext, String.format("%s is not an allowed value", input));
            throw new ValidationException(failureStatus);
        }
    }

}
