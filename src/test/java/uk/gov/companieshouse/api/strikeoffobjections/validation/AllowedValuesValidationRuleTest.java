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
class AllowedValuesValidationRuleTest {

    private static final List<Long> ALLOWED_VALUES = Arrays.asList(10L, 200L, 3000L);
    private static final String LOG_CONTEXT = "context";
    private static final EligibilityStatus FAILURE_STATUS =
            EligibilityStatus.INELIGIBLE_NO_DISSOLUTION_ACTION;

    @Mock
    private ApiLogger apiLogger;

    private AllowedValuesValidationRule<Long> allowedValuesValidationRule;

    @BeforeEach
    void setup() {
        allowedValuesValidationRule =
                new AllowedValuesValidationRule<>(ALLOWED_VALUES, FAILURE_STATUS, apiLogger);
    }

    @Test
    void validateTest() throws ValidationException {
        allowedValuesValidationRule.validate(3000L, LOG_CONTEXT);
    }

    @Test
    void validateThrowsExceptionTest() {
        ValidationException ve = assertThrows(
                ValidationException.class, () -> allowedValuesValidationRule.validate(20L, LOG_CONTEXT));

        assertEquals(FAILURE_STATUS.getObjectionStatus(), ve.getObjectionStatus());
    }

    @Test
    void validateThrowsExceptionWhenNullIsInputTest() {
        ValidationException ve = assertThrows(
                ValidationException.class, () -> allowedValuesValidationRule.validate(null, LOG_CONTEXT));

        assertEquals(FAILURE_STATUS.getObjectionStatus(), ve.getObjectionStatus());
    }
}
