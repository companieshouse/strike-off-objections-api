package uk.gov.companieshouse.api.strikeoffobjections.validation;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.eligibility.EligibilityStatus;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

import java.util.Arrays;
import java.util.List;

import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

@Unit
@ExtendWith(MockitoExtension.class)
class ActionCodeValidatorTest {

    private static final String LOG_CONTEXT = "abc";

    @Mock
    private ApiLogger apiLogger;

    @Mock
    private DisallowedValuesValidationRule<Long> ruleA;

    @Mock
    private AllowedValuesValidationRule<Long> ruleB;

    @Mock
    private DisallowedValuesValidationRule<Long> ruleC;

    private ActionCodeValidator actionCodeValidator;

    @BeforeEach
    void setup() {
        List<ValidationRule<Long>> rules = Arrays.asList(ruleA, ruleB, ruleC);

        actionCodeValidator = new ActionCodeValidator(rules, apiLogger);
    }

    @Test
    void validateCallsInOrderTest() throws ValidationException {
        Long actionCode = 123L;
        actionCodeValidator.validate(actionCode, LOG_CONTEXT);

        InOrder ruleOrder = inOrder(ruleA, ruleB, ruleC);
        ruleOrder.verify(ruleA).validate(actionCode, LOG_CONTEXT);
        ruleOrder.verify(ruleB).validate(actionCode, LOG_CONTEXT);
        ruleOrder.verify(ruleC).validate(actionCode, LOG_CONTEXT);
    }

    @Test
    void validateThrowsExceptionTest() throws ValidationException {
        Long actionCode = 123L;
        ValidationException ve = new ValidationException(EligibilityStatus.INELIGIBLE_COMPANY_STRUCK_OFF);
        doThrow(ve).when(ruleB).validate(actionCode, LOG_CONTEXT);

        Assertions.assertThrows(
                ValidationException.class,
                () -> actionCodeValidator.validate(actionCode, LOG_CONTEXT));

        verify(ruleC, never()).validate(actionCode, LOG_CONTEXT);
    }
}
