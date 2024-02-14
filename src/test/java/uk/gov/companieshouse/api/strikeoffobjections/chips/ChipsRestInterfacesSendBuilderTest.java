package uk.gov.companieshouse.api.strikeoffobjections.chips;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

@Unit
class ChipsRestInterfacesSendBuilderTest {

    @Test
    void testWithSourceAppId() {
        var appId = "app";

        ChipsRestInterfacesSend message =
                new ChipsRestInterfacesSendBuilder().withSourceAppId(appId).build();

        assertEquals(appId, message.getAppId());
    }

    @Test
    void testWithMessageId() {
        var messageId = "12345";

        ChipsRestInterfacesSend message =
                new ChipsRestInterfacesSendBuilder().withMessageId(messageId).build();

        assertEquals(messageId, message.getMessageId());
    }

    @Test
    void testWithData() {
        var data = "{some:data,and:more}";

        ChipsRestInterfacesSend message =
                new ChipsRestInterfacesSendBuilder().withData(data).build();

        assertEquals(data, message.getData());
    }

    @Test
    void testWithCreatedAtTimestampInSeconds() {
        var timestamp = "123456789";

        ChipsRestInterfacesSend message = new ChipsRestInterfacesSendBuilder()
                .withCreatedAtTimestampInSeconds(timestamp)
                .build();

        assertEquals(timestamp, message.getCreatedAt());
    }

    @Test
    void testWithChipsRestEndpoint() {
        var endpoint = "/rest-interfaces/generic/objections";

        ChipsRestInterfacesSend message =
                new ChipsRestInterfacesSendBuilder().withChipsRestEndpoint(endpoint).build();

        assertEquals(endpoint, message.getChipsRestEndpoint());
    }

    @Test
    void testWithAttemptNumber() {
        var attempt = 1;

        ChipsRestInterfacesSend message =
                new ChipsRestInterfacesSendBuilder().withAttemptNumber(attempt).build();

        assertEquals(attempt, message.getAttempt());
    }
}
