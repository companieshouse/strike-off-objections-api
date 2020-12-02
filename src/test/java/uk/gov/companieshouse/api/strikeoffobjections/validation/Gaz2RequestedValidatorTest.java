package uk.gov.companieshouse.api.strikeoffobjections.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.strikeoffobjections.client.OracleQueryClient;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class Gaz2RequestedValidatorTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final long GAZ1_ACTION_CODE = 5000L;
    private static final long NON_GAZ1_ACTION_CODE = 1000L;
    private static final String REQUEST_ID = "87654321";

    @Mock
    private OracleQueryClient oracleQueryClient;

    @Mock
    private ApiLogger apiLogger;

    @InjectMocks
    private Gaz2RequestedValidator gaz2RequestedValidator;

    @BeforeEach
    void init() {
        ReflectionTestUtils.setField(gaz2RequestedValidator, "gaz1ActionCode", GAZ1_ACTION_CODE);
    }

    @Test
    void validatePassTest() throws ValidationException {
        when(oracleQueryClient.getRequestedGaz2(COMPANY_NUMBER, REQUEST_ID)).thenReturn(null);

        gaz2RequestedValidator.validate(COMPANY_NUMBER, GAZ1_ACTION_CODE, REQUEST_ID);

        verify(oracleQueryClient, times(1)).getRequestedGaz2(COMPANY_NUMBER, REQUEST_ID);
    }

    @Test
    void verifyOracleIsNotCalledIfActionCodeNotGaz1() throws ValidationException {
        gaz2RequestedValidator.validate(COMPANY_NUMBER, NON_GAZ1_ACTION_CODE, REQUEST_ID);

        verify(oracleQueryClient, times(0)).getRequestedGaz2(COMPANY_NUMBER, REQUEST_ID);
    }

    @Test
    void validateFailsValidationGaz2RequestedTest() {
        when(oracleQueryClient.getRequestedGaz2(COMPANY_NUMBER, REQUEST_ID)).thenReturn("Some value");

        assertThrows(ValidationException.class,
                () -> gaz2RequestedValidator.validate(COMPANY_NUMBER, GAZ1_ACTION_CODE, REQUEST_ID));

        verify(oracleQueryClient, times(1)).getRequestedGaz2(COMPANY_NUMBER, REQUEST_ID);
    }
}