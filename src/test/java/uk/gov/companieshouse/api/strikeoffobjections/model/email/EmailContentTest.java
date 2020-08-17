package uk.gov.companieshouse.api.strikeoffobjections.model.email;

import com.fasterxml.jackson.core.JsonProcessingException;

import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Unit
class EmailContentTest {

    private static final String ORIGINATING_APP_ID = "APP_ID";
    private static final String MESSAGE_ID = "MESSAGE_ID";
    private static final String MESSAGE_TYPE = "MESSAGE_TYPE";
    private static final String EMAIL_ADDRESS = "test@test.com";
    private static final LocalDateTime CREATED_AT =
            LocalDateTime.of(2020, 1, 1, 0, 0);
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
        assertEquals(Utils.getDummyEmailData(), emailContent.getData());
        assertEquals(EMAIL_ADDRESS, emailContent.getEmailAddress());
        assertEquals(CREATED_AT, emailContent.getCreatedAt());
    }
}