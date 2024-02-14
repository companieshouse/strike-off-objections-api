package uk.gov.companieshouse.api.strikeoffobjections.validation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.eligibility.EligibilityStatus;

@Unit
@ExtendWith(MockitoExtension.class)
class DisallowedValuesValidationRuleTest {

    private static final List<Long> DISALLOWED_VALUES = Arrays.asList(4000L, 5000L, 6000L);
    private static final String LOG_CONTEXT = "context";
    private static final EligibilityStatus FAILURE_STATUS =
            EligibilityStatus.INELIGIBLE_COMPANY_STRUCK_OFF;

    @Mock
    private ApiLogger apiLogger;

    private DisallowedValuesValidationRule<Long> disallowedValuesValidationRule;

    @BeforeEach
    void setup() {
        disallowedValuesValidationRule =
                new DisallowedValuesValidationRule<>(DISALLOWED_VALUES, FAILURE_STATUS, apiLogger);
    }

    @Test
    void validateTest() throws ValidationException {
        disallowedValuesValidationRule.validate(3000L, LOG_CONTEXT);
    }

    @Test
    void validateIsOkWhenNullIsInputTest() throws ValidationException {
        disallowedValuesValidationRule.validate(null, LOG_CONTEXT);
    }

    @Test
    void validateThrowsExceptionTest() {
        ValidationException ve = assertThrows(
                ValidationException.class,
                () -> disallowedValuesValidationRule.validate(5000L, LOG_CONTEXT));

        assertEquals(FAILURE_STATUS.getObjectionStatus(), ve.getObjectionStatus());
    }
}
