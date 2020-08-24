package uk.gov.companieshouse.api.strikeoffobjections.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.validation.ActionCodeValidator;
import uk.gov.companieshouse.api.strikeoffobjections.validation.AllowedValuesValidationRule;
import uk.gov.companieshouse.api.strikeoffobjections.validation.DisallowedValuesValidationRule;
import uk.gov.companieshouse.api.strikeoffobjections.validation.ValidationRule;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ValidationConfig {

    private static final List<Object> COMPANY_STRUCK_OFF_ACTION_CODES = Arrays.asList("90", "9000", "9100");
    private static final List<Object> NO_CURRENT_DISSOLUTION = Arrays.asList("4100", "4300", "4400", "5000");

    @Bean
    public ActionCodeValidator getActionCodeValidator() {
        List<ValidationRule> rules = Arrays.asList(
                new DisallowedValuesValidationRule(COMPANY_STRUCK_OFF_ACTION_CODES, ObjectionStatus.INELIGIBLE_COMPANY_STRUCK_OFF),
                new AllowedValuesValidationRule(NO_CURRENT_DISSOLUTION, ObjectionStatus.INELIGIBLE_NO_CURRENT_DISSOLUTION)
        );
        return new ActionCodeValidator(rules);
    }
}
