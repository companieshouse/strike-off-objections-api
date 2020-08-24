package uk.gov.companieshouse.api.strikeoffobjections.validation;

import java.util.List;

public class ActionCodeValidator {
    private List<ValidationRule> rules;

    public ActionCodeValidator(List<ValidationRule> rules) {
        this.rules = rules;
    }

    public void validate(String actionCode) throws ValidationException {
        // TODO logging
        for (ValidationRule rule: rules) {
            rule.validate(actionCode);
        }
    }
}
