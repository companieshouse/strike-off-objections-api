package uk.gov.companieshouse.api.strikeoffobjections.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.validation.AllowedValuesValidationRule;
import uk.gov.companieshouse.api.strikeoffobjections.validation.DisallowedValuesValidationRule;
import uk.gov.companieshouse.api.strikeoffobjections.validation.ValidationRule;

import java.util.Arrays;
import java.util.List;

@Configuration
public class ValidationConfig {

    @Value("#{'${ACTION_CODES_COMPANY_STRUCK_OFF}'.split(',')}")
    private List<Long> ACTION_CODES_COMPANY_STRUCK_OFF;

    @Value("#{'${ACTION_CODES_NO_DISSOLUTION_ACTION}'.split(',')}")
    private List<Long> ACTION_CODES_NO_DISSOLUTION_ACTION;

    @Autowired
    private ApiLogger apiLogger;

    @Bean
    public List<ValidationRule> getActionCodeValidationRules() {
        return Arrays.asList(
                new DisallowedValuesValidationRule<>(
                        ACTION_CODES_COMPANY_STRUCK_OFF,
                        ObjectionStatus.INELIGIBLE_COMPANY_STRUCK_OFF,
                        apiLogger),
                new AllowedValuesValidationRule<>(
                        ACTION_CODES_NO_DISSOLUTION_ACTION,
                        ObjectionStatus.INELIGIBLE_NO_CURRENT_DISSOLUTION,
                        apiLogger)
        );
    }
}
