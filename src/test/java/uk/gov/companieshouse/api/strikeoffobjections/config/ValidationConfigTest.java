package uk.gov.companieshouse.api.strikeoffobjections.config;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.validation.AllowedValuesValidationRule;
import uk.gov.companieshouse.api.strikeoffobjections.validation.DisallowedValuesValidationRule;
import uk.gov.companieshouse.api.strikeoffobjections.validation.ValidationRule;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Unit
@ExtendWith(MockitoExtension.class)
class ValidationConfigTest {

    private static final List<Long> STRUCK_OFF_VALUES = Arrays.asList(10L, 200L, 3000L);
    private static final List<Long> NO_DISS_VALUES = Arrays.asList(10L, 200L, 3000L);

    @Mock
    private ApiLogger apiLogger;

    private ValidationConfig validationConfig;

    @BeforeEach
    void setup() {
        validationConfig = new ValidationConfig();
        ReflectionTestUtils.setField(validationConfig, "ACTION_CODES_COMPANY_STRUCK_OFF", STRUCK_OFF_VALUES);
        ReflectionTestUtils.setField(validationConfig, "ACTION_CODES_NO_DISSOLUTION_ACTION", NO_DISS_VALUES);
        ReflectionTestUtils.setField(validationConfig, "apiLogger", apiLogger);
    }

    @Test
    void getActionCodeValidationRulesTest() {
        List<ValidationRule> rules = validationConfig.getActionCodeValidationRules();

        assertEquals(DisallowedValuesValidationRule.class, rules.get(0).getClass());
        assertEquals(AllowedValuesValidationRule.class, rules.get(1).getClass());
    }
}
