package uk.gov.companieshouse.api.strikeoffobjections.config;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.validation.AllowedValuesValidationRule;
import uk.gov.companieshouse.api.strikeoffobjections.validation.DisallowedValuesValidationRule;
import uk.gov.companieshouse.api.strikeoffobjections.validation.ValidationRule;

@Unit
@ExtendWith(SpringExtension.class)
@ContextConfiguration(classes = ValidationConfig.class)
@TestPropertySource(
        properties = {"ACTION_CODES_COMPANY_STRUCK_OFF=10,20", "ACTION_CODES_STRIKE_OFF_NOTICE=30,40"})
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class ValidationConfigTest {

    @MockBean
    private ApiLogger apiLogger;

    @Autowired
    private ValidationConfig validationConfig;

    @Test
    @Order(1)
    // need this to run first as we want to capture the call to apiLogger in the postConstruct of
    // ValidationConfig
    // the mockBean apiLogger gets reset after each test so if we don't run this first, we'll lose the
    // record of
    // it being called in the postConstruct of ValidationConfig (when it gets injected)
    void logActionCodeValidatorConfigValuesTest() {

        ArgumentCaptor<String> loggerArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(apiLogger, times(2)).info(loggerArgumentCaptor.capture());

        List<String> logMessageList = loggerArgumentCaptor.getAllValues();
        assertTrue(logMessageList.get(0).contains("ACTION_CODES_COMPANY_STRUCK_OFF = [10, 20]"));
        assertTrue(logMessageList.get(1).contains("ACTION_CODES_STRIKE_OFF_NOTICE = [30, 40]"));
    }

    @Test
    void getActionCodeValidationRulesTest() {
        List<ValidationRule<Long>> rules = validationConfig.getActionCodeValidationRules();

        assertEquals(DisallowedValuesValidationRule.class, rules.get(0).getClass());
        assertEquals(AllowedValuesValidationRule.class, rules.get(1).getClass());
    }
}
