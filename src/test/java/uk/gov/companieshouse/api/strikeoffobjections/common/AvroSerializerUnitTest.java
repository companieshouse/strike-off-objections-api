package uk.gov.companieshouse.api.strikeoffobjections.common;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.time.LocalDateTime;
import org.apache.avro.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.email.EmailContent;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

@Unit
@ExtendWith(MockitoExtension.class)
class AvroSerializerUnitTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2020, 1, 1, 5, 0);
    private static final String EMAIL_TEMPLATE_MESSAGE_TYPE = "test_confirmation_email";
    private static final String MESSAGE_ID = "abc";
    private static final String APP_ID = "strike-off-objections-api";
    private static final String RECIPIENT = "example@test.co.uk";
    private static final String EXPECTED_CREATED_AT = "01 Jan 2020 05:00:00";
    private static final int ATTEMPT = 1;
    private static final String CHIPS_REST_ENDPOINT = "/rest/chipRest/etc";
    private static final String DUMMY_DATA = "{dummy:data,more:data}";

    @InjectMocks
    private AvroSerializer avroSerializer;

    @Test
    void testAvroSerializerForEmailContent() throws IOException {
        Schema schema =
                Utils.getDummySchema(this.getClass().getClassLoader().getResource("email/email-send.avsc"));
        EmailContent emailContent = Utils.buildEmailContent(
                APP_ID,
                MESSAGE_ID,
                EMAIL_TEMPLATE_MESSAGE_TYPE,
                Utils.getDummyEmailData(),
                RECIPIENT,
                CREATED_AT);

        byte[] byteArray = avroSerializer.serialize(emailContent, schema);
        String result = new String(byteArray);

        assertTrue(result.contains(APP_ID));
        assertTrue(result.contains(MESSAGE_ID));
        assertTrue(result.contains(Utils.getDummyEmailData().get("to").toString()));
        assertTrue(result.contains(Utils.getDummyEmailData().get("subject").toString()));
        assertTrue(result.contains(Utils.getDummyEmailData().get("company_name").toString()));
        assertTrue(result.contains(Utils.getDummyEmailData().get("company_number").toString()));
        assertTrue(result.contains(Utils.getDummyEmailData().get("reason").toString()));
        assertTrue(result.contains(RECIPIENT));
        assertTrue(result.contains(EXPECTED_CREATED_AT));
    }

    @Test
    void testAvroSerializerForSpecificContent() throws IOException {
        String createdAt = CREATED_AT.toString();

        ChipsRestInterfacesSend chipsMessage = new ChipsRestInterfacesSend();
        chipsMessage.setAttempt(ATTEMPT);
        chipsMessage.setChipsRestEndpoint(CHIPS_REST_ENDPOINT);
        chipsMessage.setCreatedAt(createdAt);
        chipsMessage.setMessageId(MESSAGE_ID);
        chipsMessage.setAppId(APP_ID);
        chipsMessage.setData(DUMMY_DATA);

        byte[] byteArray = avroSerializer.serialize(chipsMessage);
        String result = new String(byteArray);

        assertTrue(result.contains(String.valueOf(ATTEMPT)));
        assertTrue(result.contains(CHIPS_REST_ENDPOINT));
        assertTrue(result.contains(createdAt));
        assertTrue(result.contains(MESSAGE_ID));
        assertTrue(result.contains(APP_ID));
        assertTrue(result.contains(DUMMY_DATA));
    }
}
