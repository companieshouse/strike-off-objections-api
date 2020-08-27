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

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;

@Configuration
public class ValidationConfig {

    private static final String CONFIG_MESSAGE_PREFIX = "CHS ENV CONFIG -";

    @Value("#{'${ACTION_CODES_COMPANY_STRUCK_OFF}'.split(',')}")
    private List<Long> companyStruckOffActionCodes;

    @Value("#{'${ACTION_CODES_STRIKE_OFF_NOTICE}'.split(',')}")
    private List<Long> strikeOffNoticeActionCodes;

    @Autowired
    private ApiLogger apiLogger;

    @Bean
    public List<ValidationRule<Long>> getActionCodeValidationRules() {
        return Arrays.asList(
                new DisallowedValuesValidationRule<>(
                        companyStruckOffActionCodes,
                        ObjectionStatus.INELIGIBLE_COMPANY_STRUCK_OFF,
                        apiLogger),
                new AllowedValuesValidationRule<>(
                        strikeOffNoticeActionCodes,
                        ObjectionStatus.INELIGIBLE_NO_DISSOLUTION_ACTION,
                        apiLogger)
        );
    }

    @PostConstruct
    private void logActionCodeValidatorConfigValues() {
        apiLogger.info(
                String.format("%s ACTION_CODES_COMPANY_STRUCK_OFF = %s", CONFIG_MESSAGE_PREFIX, companyStruckOffActionCodes));
        apiLogger.info(
                String.format("%s ACTION_CODES_STRIKE_OFF_NOTICE = %s", CONFIG_MESSAGE_PREFIX, strikeOffNoticeActionCodes));
    }
}
