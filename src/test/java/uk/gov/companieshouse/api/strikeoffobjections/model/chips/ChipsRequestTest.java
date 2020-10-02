package uk.gov.companieshouse.api.strikeoffobjections.model.chips;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;

import java.util.ArrayList;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ChipsRequestTest {

    private static final String REQUEST_ID = "test123";
    private static final String COMPANY_NUMBER = "12345678";
    private static final List<Attachment> ATTACHMENTS = new ArrayList<>();
    private static final String CUSTOMER_EMAIL = "test123@ch.gov.uk";
    private static final String REASON = "This is a test";
    private static final String DOWNLOAD_URL_PREFIX = "http://chs-test-web:4000/strike-off-objections/download";

    @Test
    void testFieldsAndAttachmentConstruction()
    {
        Utils.setTestAttachmentsWithLinks(ATTACHMENTS);
        ChipsRequest chipsRequest = new ChipsRequest(
                REQUEST_ID,
                COMPANY_NUMBER,
                ATTACHMENTS,
                REQUEST_ID,
                CUSTOMER_EMAIL,
                REASON,
                DOWNLOAD_URL_PREFIX
        );


        assertEquals(COMPANY_NUMBER, chipsRequest.getCompanyNumber());
        assertEquals(String.format("%s/url1/download", DOWNLOAD_URL_PREFIX),
                chipsRequest.getAttachments().get("TestAttachment1"));
        assertEquals(String.format("%s/url2/download", DOWNLOAD_URL_PREFIX),
                chipsRequest.getAttachments().get("TestAttachment2"));
        assertEquals(REQUEST_ID, chipsRequest.getReferenceNumber());
        assertEquals(CUSTOMER_EMAIL, chipsRequest.getCustomerEmail());
        assertEquals(REASON, chipsRequest.getReason());
    }
}
