package uk.gov.companieshouse.api.strikeoffobjections.client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

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
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

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
}