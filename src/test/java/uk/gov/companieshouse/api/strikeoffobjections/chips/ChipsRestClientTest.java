package uk.gov.companieshouse.api.strikeoffobjections.chips;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
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
import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;

@Unit
@ExtendWith(MockitoExtension.class)
class ChipsRestClientTest {
    private static final String REQUEST_ID = "test123";
    private static final String OBJECTION_ID = "OBJECTION_ID";
    private static final String COMPANY_NUMBER = "12345678";
    private static final List<Attachment> ATTACHMENTS = new ArrayList<>();
    private static final String FULL_NAME = "Joe Bloggs";
    private static final Boolean SHARE_IDENTITY = true;
    private static final String CUSTOMER_EMAIL = "test123@ch.gov.uk";
    private static final String REASON = "This is a test";
    private static final String CHIPS_REST_URL = "test.url";
    private static final String DOWNLOAD_URL_PREFIX =
            "http://chs-test-web:4000/strike-off-objections/download";

    @InjectMocks private ChipsRestClient chipsRestClient;

    @Mock private ApiLogger apiLogger;

    @Mock private RestTemplate restTemplate;

    @Test
    void testSendMessageToChips() {
        Utils.setTestAttachmentsWithLinks(ATTACHMENTS);
        ReflectionTestUtils.setField(chipsRestClient, "chipsRestUrl", CHIPS_REST_URL);
        ChipsRequest chipsRequest =
                new ChipsRequest.Builder()
                        .objectionId(OBJECTION_ID)
                        .companyNumber(COMPANY_NUMBER)
                        .attachments(DOWNLOAD_URL_PREFIX, ATTACHMENTS)
                        .referenceNumber(OBJECTION_ID)
                        .fullName(FULL_NAME)
                        .shareIdentity(SHARE_IDENTITY)
                        .customerEmail(CUSTOMER_EMAIL)
                        .reason(REASON)
                        .build();

        when(restTemplate.postForEntity(CHIPS_REST_URL, chipsRequest, String.class))
                .thenReturn(new ResponseEntity<>(HttpStatus.OK));
        chipsRestClient.sendToChips(REQUEST_ID, chipsRequest);
        verify(restTemplate, times(1)).postForEntity(CHIPS_REST_URL, chipsRequest, String.class);

        Map<String, Object> logMap = new HashMap<>();
        logMap.put("chipsRestUrl", "test.url");
        verify(apiLogger)
                .infoContext(
                        REQUEST_ID, String.format("Posting %s to CHIPS rest interfaces", chipsRequest), logMap);
        verify(apiLogger).infoContext(REQUEST_ID, "Sent data to CHIPS, received status code: 200 OK");
    }
}
