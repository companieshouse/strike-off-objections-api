package uk.gov.companieshouse.api.strikeoffobjections.chips;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.ReflectionUtils;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class ChipsClientTest {

    private static final String REQUEST_ID = "test123";
    private static final String COMPANY_NUMBER = "12345678";
    private static final String CHIPS_REST_URL = "test.url";

    @InjectMocks
    private ChipsClient chipsClient;

    @Mock
    private ApiLogger apiLogger;

    @Mock
    private RestTemplate restTemplate;

    @Test
    void testSendMessageToChips(){
        ReflectionTestUtils.setField(chipsClient, "chipsRestUrl", CHIPS_REST_URL);
        ChipsRequest chipsRequest = new ChipsRequest(
                REQUEST_ID,
                COMPANY_NUMBER
        );

        when(restTemplate.postForEntity(CHIPS_REST_URL, chipsRequest, String.class)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        chipsClient.sendToChips(REQUEST_ID, chipsRequest);
        verify(restTemplate, times(1)).postForEntity(CHIPS_REST_URL, chipsRequest, String.class);
    }
}
