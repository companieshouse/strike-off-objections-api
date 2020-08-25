package uk.gov.companieshouse.api.strikeoffobjections.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

@Unit
@ExtendWith(MockitoExtension.class)
class AllowedValuesValidationRuleTest {

    private static final List<Long> ALLOWED_VALUES = Arrays.asList(10L, 200L, 3000L);
    private static final String LOG_CONTEXT = "context";
    private static final ObjectionStatus FAILURE_STATUS = ObjectionStatus.INELIGIBLE_NO_CURRENT_DISSOLUTION;

    @Mock
    private ApiLogger apiLogger;

    private AllowedValuesValidationRule<Long> allowedValuesValidationRule;

    @BeforeEach
    void setup() {
        allowedValuesValidationRule = new AllowedValuesValidationRule<>(
                ALLOWED_VALUES,
                FAILURE_STATUS,
                apiLogger
        );
    }

    @Test
    void validateTest() throws ValidationException {
        allowedValuesValidationRule.validate(3000L, LOG_CONTEXT);
    }

    @Test
    void validateThrowsExceptionTest() {
        ValidationException ve = assertThrows(
                ValidationException.class,
                () -> allowedValuesValidationRule.validate(20L, LOG_CONTEXT));

        assertEquals(FAILURE_STATUS, ve.getStatus());
    }
}
