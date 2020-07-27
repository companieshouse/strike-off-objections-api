package uk.gov.companieshouse.api.strikeoffobjections.model.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailContentTest {

    private static final String ORIGINATING_APP_ID = "APP_ID";
    private static final String MESSAGE_ID = "MESSAGE_ID";
    private static final String MESSAGE_TYPE = "MESSAGE_TYPE";
    private static final String EMAIL_ADDRESS = "test@test.com";
    private static final String CREATED_AT = "CREATED_AT";
    private static final String DATA = "" +
            "{\"reason\":\"Testing this\"," +
            "\"company_number\":\"00001111\"," +
            "\"subject\":\"Test objection submitted\"," +
            "\"company_name\":\"TEST COMPANY\"," +
            "\"to\":\"example@test.co.uk\"}";

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Test
    void emailBuilderTest() throws JsonProcessingException {
        EmailContent emailContent = new EmailContent.Builder()
                .withOriginatingAppId(ORIGINATING_APP_ID)
                .withEmailAddress(EMAIL_ADDRESS)
                .withCreatedAt(CREATED_AT)
                .withData(Utils.getDummyEmailData())
                .withMessageId(MESSAGE_ID)
                .withMessageType(MESSAGE_TYPE)
                .build();

        assertEquals(ORIGINATING_APP_ID, emailContent.getOriginatingAppId());
        assertEquals(MESSAGE_ID, emailContent.getMessageId());
        assertEquals(MESSAGE_TYPE, emailContent.getMessageType());
        assertEquals(DATA, emailContent.getData());
        assertEquals(EMAIL_ADDRESS, emailContent.getEmailAddress());
        assertEquals(CREATED_AT, emailContent.getCreatedAt());
    }
}