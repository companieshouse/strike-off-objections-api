package uk.gov.companieshouse.api.strikeoffobjections.model.model;

import org.junit.jupiter.api.Test;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class EmailContentTest {

    private static final String ORIGINATING_APP_ID = "APP_ID";
    private static final String MESSAGE_ID = "MESSAGE_ID";
    private static final String MESSAGE_TYPE = "MESSAGE_TYPE";
    private static final Map<String, Object> DATA = Collections.singletonMap("field_1", "value_1");
    private static final String EMAIL_ADDRESS = "test@test.com";
    private static final String CREATED_AT = "CREATED_AT";

    @Test
    void emailBuilderTest() {
        EmailContent emailContent = new EmailContent.Builder()
                .withOriginatingAppId(ORIGINATING_APP_ID)
                .withEmailAddress(EMAIL_ADDRESS)
                .withCreatedAt(CREATED_AT)
                .withData(DATA)
                .withMessageId(MESSAGE_ID)
                .withMessageType(MESSAGE_TYPE)
                .build();

        assertEquals(emailContent.getOriginatingAppId(), ORIGINATING_APP_ID);
        assertEquals(emailContent.getMessageId(), MESSAGE_ID);
        assertEquals(emailContent.getMessageType(), MESSAGE_TYPE);
        assertEquals(emailContent.getData(), DATA);
        assertEquals(emailContent.getEmailAddress(), EMAIL_ADDRESS);
        assertEquals(emailContent.getCreatedAt(), CREATED_AT);
    }
}