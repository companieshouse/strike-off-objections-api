package uk.gov.companieshouse.api.strikeoffobjections.validation;

public interface ValidationRule {
    void validate(Object input) throws ValidationException;
}
