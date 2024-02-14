package uk.gov.companieshouse.api.strikeoffobjections.validation;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;

@Component
public class ActionCodeValidator {
    private List<ValidationRule<Long>> actionCodeValidationRules;
    private ApiLogger apiLogger;

    @Autowired
    public ActionCodeValidator(
            List<ValidationRule<Long>> actionCodeValidationRules, ApiLogger apiLogger) {
        this.actionCodeValidationRules = actionCodeValidationRules;
        this.apiLogger = apiLogger;
    }

    public void validate(Long actionCode, String logContext) throws ValidationException {
        apiLogger.debugContext(
                logContext,
                String.format("Running action code validation rules for action code %s", actionCode));
        for (ValidationRule<Long> rule : actionCodeValidationRules) {
            rule.validate(actionCode, logContext);
        }
    }
}
