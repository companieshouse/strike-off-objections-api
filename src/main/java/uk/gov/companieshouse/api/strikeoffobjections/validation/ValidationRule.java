package uk.gov.companieshouse.api.strikeoffobjections.validation;

public interface ValidationRule<T> {
    void validate(T input, String logContext) throws ValidationException;
}
