package uk.gov.companieshouse.api.strikeoffobjections.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;

import java.util.List;

@Component
public class ActionCodeValidator {
    private List<ValidationRule> actionCodeValidationRules;
    private ApiLogger apiLogger;

    @Autowired
    public ActionCodeValidator(List<ValidationRule> actionCodeValidationRules,
                               ApiLogger apiLogger) {
        this.actionCodeValidationRules = actionCodeValidationRules;
        this.apiLogger = apiLogger;
    }

    public void validate(String actionCode, String logContext) throws ValidationException {
        apiLogger.debugContext(
                logContext,
                String.format("Running action code validation rules for action code %s", actionCode));
        for (ValidationRule rule: actionCodeValidationRules) {
            rule.validate(actionCode, logContext);
        }
    }
}
