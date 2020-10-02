package uk.gov.companieshouse.api.strikeoffobjections.chips;

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

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class ChipsClientTest {

    private static final String OBJECTION_ID = "test123";
    private static final String COMPANY_NUMBER = "12345678";
    private static final List<Attachment> ATTACHMENTS = new ArrayList<>();
    private static final String CUSTOMER_EMAIL = "test123@ch.gov.uk";
    private static final String REASON = "This is a test";
    private static final String CHIPS_REST_URL = "test.url";
    private static final String DOWNLOAD_URL_PREFIX = "http://chs-test-web:4000/strike-off-objections/download";

    @InjectMocks
    private ChipsClient chipsClient;

    @Mock
    private ApiLogger apiLogger;

    @Mock
    private RestTemplate restTemplate;

    @Test
    void testSendMessageToChips() {
        Utils.setTestAttachmentsWithLinks(ATTACHMENTS);
        ReflectionTestUtils.setField(chipsClient, "chipsRestUrl", CHIPS_REST_URL);
        ChipsRequest chipsRequest = new ChipsRequest(
                OBJECTION_ID,
                COMPANY_NUMBER,
                ATTACHMENTS,
                OBJECTION_ID,
                CUSTOMER_EMAIL,
                REASON,
                DOWNLOAD_URL_PREFIX
        );

        when(restTemplate.postForEntity(CHIPS_REST_URL, chipsRequest, String.class)).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        chipsClient.sendToChips(OBJECTION_ID, chipsRequest);
        verify(restTemplate, times(1)).postForEntity(CHIPS_REST_URL, chipsRequest, String.class);
    }

}
