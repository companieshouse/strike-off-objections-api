package uk.gov.companieshouse.api.strikeoffobjections.client;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.UnsafeUrlException;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@Unit
@ExtendWith(MockitoExtension.class)
class OracleQueryClientTest {

    private static final String DUMMY_URL = "http://test";
    private static final String COMPANY_NUMBER = "12345678";
    private static final Long ACTION_CODE = 88L;
    private static final String GAZ2_TRANSACTION = "GAZ2";
    private static final String REQUEST_ID = "1234";

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApiLogger apiLogger;

    @InjectMocks
    private OracleQueryClient oracleQueryClient;

    private OracleQueryClient corruptibleClient = spy(new OracleQueryClient(restTemplate, apiLogger));

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(oracleQueryClient, "oracleQueryApiUrl", DUMMY_URL);
    }

    @Test
    void testUrlCorrectlyConstructedAndActionCodeReturned() {
        when(restTemplate.getForEntity(DUMMY_URL + "/company/" + COMPANY_NUMBER + "/action-code", Long.class))
                .thenReturn(new ResponseEntity<>(ACTION_CODE, HttpStatus.OK));

        Long actionCode = oracleQueryClient.getCompanyActionCode(COMPANY_NUMBER, REQUEST_ID);

        assertEquals(ACTION_CODE, actionCode);
    }

    @Test
    void testUrlCorrectlyConstructedAndGaz2Returned() {
        when(restTemplate.getForEntity(DUMMY_URL + "/company/" + COMPANY_NUMBER + "/gaz2-requested", String.class))
                .thenReturn(new ResponseEntity<>(GAZ2_TRANSACTION, HttpStatus.OK));

        String gaz2Transaction = oracleQueryClient.getRequestedGaz2(COMPANY_NUMBER, REQUEST_ID);

        assertEquals(GAZ2_TRANSACTION, gaz2Transaction);
    }

    @Test
    void testActionCodeWithUnaccountedCompanyNumber() {
        ReflectionTestUtils.setField(corruptibleClient, "oracleQueryApiUrl", DUMMY_URL);
        ReflectionTestUtils.setField(corruptibleClient, "apiLogger", apiLogger);

        doReturn("url-is-corrupted-by-company-number")
                .when(corruptibleClient)
                .formatUrl(COMPANY_NUMBER, "action-code");

        assertThrows(UnsafeUrlException.class,
                () -> corruptibleClient.getCompanyActionCode(COMPANY_NUMBER, REQUEST_ID));
    }

    @Test
    void testGaz2WithUnaccountedCompanyNumber() {
        ReflectionTestUtils.setField(corruptibleClient, "oracleQueryApiUrl", DUMMY_URL);
        ReflectionTestUtils.setField(corruptibleClient, "apiLogger", apiLogger);

        doReturn("url-is-corrupted-by-company-number")
                .when(corruptibleClient)
                .formatUrl(COMPANY_NUMBER, "gaz2-requested");

        assertThrows(UnsafeUrlException.class,
                () -> corruptibleClient.getRequestedGaz2(COMPANY_NUMBER, REQUEST_ID));
    }
}