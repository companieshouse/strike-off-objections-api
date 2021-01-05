package uk.gov.companieshouse.api.strikeoffobjections.model.chipsrestinterfaceskafkamessage;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;

@Unit
public class ChipsRestInterfacesKafkaMessageContentTest {

    private static final String APP_ID = "APP_ID";
    private static final String MESSAGE_ID = "MESSAGE_ID";
    private static final String CHIPS_REST_ENDPOINT = "ENDPOINT";
    private static final LocalDateTime CREATED_AT =
            LocalDateTime.of(2020, 1, 1, 0, 0);

    @Test
    void toStringTest() throws JsonProcessingException {
        ChipsRestInterfacesKafkaMessageContent chipsRestInterfacesKafkaMessageContent = new ChipsRestInterfacesKafkaMessageContent();
        chipsRestInterfacesKafkaMessageContent.setAppId(APP_ID);
        chipsRestInterfacesKafkaMessageContent.setMessageId(MESSAGE_ID);
        chipsRestInterfacesKafkaMessageContent.setData(Utils.getDummyEmailData());
        chipsRestInterfacesKafkaMessageContent.setChipsRestEndpoint(CHIPS_REST_ENDPOINT);
        chipsRestInterfacesKafkaMessageContent.setCreatedAt(CREATED_AT);
        String chipsRestInterfacesKafkaMessageContentString = chipsRestInterfacesKafkaMessageContent.toString();

        assertTrue(chipsRestInterfacesKafkaMessageContentString.contains(APP_ID));
        assertTrue(chipsRestInterfacesKafkaMessageContentString.contains(MESSAGE_ID));
        assertTrue(chipsRestInterfacesKafkaMessageContentString.contains(Utils.getDummyEmailData().toString()));
        assertTrue(chipsRestInterfacesKafkaMessageContentString.contains(CHIPS_REST_ENDPOINT));
        assertTrue(chipsRestInterfacesKafkaMessageContentString.contains(CREATED_AT.toString()));
        assertTrue(chipsRestInterfacesKafkaMessageContentString.contains(APP_ID));

    }
}