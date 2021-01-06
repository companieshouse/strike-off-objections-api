package uk.gov.companieshouse.api.strikeoffobjections.model.chipsrestinterfacesconsumer;

import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;

import java.time.LocalDateTime;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Unit
public class ChipsRestInterfacesConsumerContentTest {

    private static final String APP_ID = "APP_ID";
    private static final String MESSAGE_ID = "MESSAGE_ID";
    private static final Map<String, Object> DATA = Utils.getDummyEmailData();
    private static final String CHIPS_REST_ENDPOINT = "ENDPOINT";
    private static final LocalDateTime CREATED_AT =
            LocalDateTime.of(2020, 1, 1, 0, 0);

    @Test
    void toStringTest() {
        ChipsRestInterfacesConsumerContent chipsRestInterfacesConsumerContent =
                new ChipsRestInterfacesConsumerContent.Builder()
                .withAppId(APP_ID)
                .withMessageId(MESSAGE_ID)
                .withData(DATA)
                .withChipsRestEndpoint(CHIPS_REST_ENDPOINT)
                .withCreatedAt(CREATED_AT)
                .build();

        String expectedOutput = String.format(
                "ChipsKafkaMessage{" + "appId='%s', messageId='%s', data='%s', chipsRestEndpoint='%s', createdAt='%s'}",
                APP_ID, MESSAGE_ID, DATA, CHIPS_REST_ENDPOINT, CREATED_AT);
        assertEquals(expectedOutput, chipsRestInterfacesConsumerContent.toString());
    }
}
