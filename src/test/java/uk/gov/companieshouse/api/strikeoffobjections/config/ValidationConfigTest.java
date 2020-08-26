package uk.gov.companieshouse.api.strikeoffobjections.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.email.EmailContent;
import uk.gov.companieshouse.api.strikeoffobjections.validation.AllowedValuesValidationRule;
import uk.gov.companieshouse.api.strikeoffobjections.validation.DisallowedValuesValidationRule;
import uk.gov.companieshouse.api.strikeoffobjections.validation.ValidationRule;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Unit
@ExtendWith(MockitoExtension.class)
class ValidationConfigTest {

    private static final List<Long> STRUCK_OFF_VALUES = Arrays.asList(10L, 200L, 3000L);
    private static final List<Long> STRIKE_OFF_NOTICE_VALUES = Arrays.asList(10L, 200L, 3000L);

    @Mock
    private ApiLogger apiLogger;

    private ValidationConfig validationConfig;

    @BeforeEach
    void setup() {
        validationConfig = new ValidationConfig();
        ReflectionTestUtils.setField(validationConfig, "companyStruckOffActionCodes", STRUCK_OFF_VALUES);
        ReflectionTestUtils.setField(validationConfig, "strikeOffNoticeActionCodes", STRIKE_OFF_NOTICE_VALUES);
    }

    @Test
    void getActionCodeValidationRulesTest() {
        List<ValidationRule<Long>> rules = validationConfig.getActionCodeValidationRules(apiLogger);

        assertEquals(DisallowedValuesValidationRule.class, rules.get(0).getClass());
        assertEquals(AllowedValuesValidationRule.class, rules.get(1).getClass());

        ArgumentCaptor<String> loggerArgumentCaptor = ArgumentCaptor.forClass(String.class);
        verify(apiLogger, times(2)).info(loggerArgumentCaptor.capture());

        List<String> logMessageList = loggerArgumentCaptor.getAllValues();
        assertTrue(logMessageList.get(0).contains(STRUCK_OFF_VALUES.toString()));
        assertTrue(logMessageList.get(1).contains(STRIKE_OFF_NOTICE_VALUES.toString()));
    }
}
