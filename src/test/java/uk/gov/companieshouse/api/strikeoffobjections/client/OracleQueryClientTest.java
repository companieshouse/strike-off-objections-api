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

import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

@Unit
@ExtendWith(MockitoExtension.class)
public class OracleQueryClientTest {

    private static final String DUMMY_URL = "http://test";
    private static final String COMPANY_NUMBER = "12345678";
    private static final Long ACTION_CODE = 88L;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private OracleQueryClient oracleQueryClient;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(oracleQueryClient, "oracleQueryApiUrl", DUMMY_URL);
    }

    @Test
    public void testUrlCorrectlyConstructedAndActionCodeReturned() {
        when(restTemplate.getForEntity(DUMMY_URL + "/company/" + COMPANY_NUMBER + "/action-code", Long.class))
                .thenReturn(new ResponseEntity<>(ACTION_CODE, HttpStatus.OK));

        Long actionCode = oracleQueryClient.getCompanyActionCode(COMPANY_NUMBER);
        
        assertEquals(ACTION_CODE, actionCode);
    }
}
