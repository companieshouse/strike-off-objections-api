package uk.gov.companieshouse.api.strikeoffobjections.chips;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

@Unit
class ChipsRestInterfacesSendBuilderTest {

    @Test
    void testWithSourceAppId() {
        String appId = "app";

        ChipsRestInterfacesSend message =
                new ChipsRestInterfacesSendBuilder().withSourceAppId(appId).build();

        assertEquals(appId, message.getAppId());
    }

    @Test
    void testWithMessageId() {
        String messageId = "12345";

        ChipsRestInterfacesSend message =
                new ChipsRestInterfacesSendBuilder().withMessageId(messageId).build();

        assertEquals(messageId, message.getMessageId());
    }

    @Test
    void testWithData() {
        String data = "{some:data,and:more}";

        ChipsRestInterfacesSend message =
                new ChipsRestInterfacesSendBuilder().withData(data).build();

        assertEquals(data, message.getData());
    }

    @Test
    void testWithCreatedAtTimestampInSeconds() {
        String timestamp = "123456789";

        ChipsRestInterfacesSend message = new ChipsRestInterfacesSendBuilder()
                .withCreatedAtTimestampInSeconds(timestamp)
                .build();

        assertEquals(timestamp, message.getCreatedAt());
    }

    @Test
    void testWithChipsRestEndpoint() {
        String endpoint = "/rest-interfaces/generic/objections";

        ChipsRestInterfacesSend message =
                new ChipsRestInterfacesSendBuilder().withChipsRestEndpoint(endpoint).build();

        assertEquals(endpoint, message.getChipsRestEndpoint());
    }

    @Test
    void testWithAttemptNumber() {
        int attempt = 1;

        ChipsRestInterfacesSend message =
                new ChipsRestInterfacesSendBuilder().withAttemptNumber(attempt).build();

        assertEquals(attempt, message.getAttempt());
    }
}
