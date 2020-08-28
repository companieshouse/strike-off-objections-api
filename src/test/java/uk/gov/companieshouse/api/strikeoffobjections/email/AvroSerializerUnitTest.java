package uk.gov.companieshouse.api.strikeoffobjections.email;

import org.apache.avro.Schema;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.email.EmailContent;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;

import java.io.IOException;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertTrue;


@Unit
@ExtendWith(MockitoExtension.class)
class AvroSerializerUnitTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2020, 1, 1, 5, 0);
    private static final String EMAIL_TEMPLATE_MESSAGE_TYPE = "test_confirmation_email";
    private static final String MESSAGE_ID = "abc";
    private static final String EMAIL_TEMPLATE_APP_ID = "filing_received_notification_sender.test_confirmation_email";
    private static final String RECIPIENT = "example@test.co.uk";
    private static final String EXPECTED_CREATED_AT = "01 Jan 2020 05:00:00";

    @InjectMocks
    private AvroSerializer avroSerializer;

    @Test
    void testAvroSerializer()
            throws IOException {
        Schema schema = Utils.getDummySchema(this.getClass().getClassLoader().getResource(
                "email/email-send.avsc"));
        EmailContent emailContent = Utils.buildEmailContent(
                EMAIL_TEMPLATE_APP_ID,
                MESSAGE_ID,
                EMAIL_TEMPLATE_MESSAGE_TYPE,
                Utils.getDummyEmailData(),
                RECIPIENT,
                CREATED_AT);
        byte[] byteArray = avroSerializer.serialize(emailContent, schema);
        String result = new String(byteArray);
        assertTrue(result.contains(EMAIL_TEMPLATE_APP_ID));
        assertTrue(result.contains(MESSAGE_ID));
        assertTrue(result.contains(EMAIL_TEMPLATE_APP_ID));
        assertTrue(result.contains(Utils.getDummyEmailData().get("to").toString()));
        assertTrue(result.contains(Utils.getDummyEmailData().get("subject").toString()));
        assertTrue(result.contains(Utils.getDummyEmailData().get("company_name").toString()));
        assertTrue(result.contains(Utils.getDummyEmailData().get("company_number").toString()));
        assertTrue(result.contains(Utils.getDummyEmailData().get("reason").toString()));
        assertTrue(result.contains(RECIPIENT));
        assertTrue(result.contains(EXPECTED_CREATED_AT));
    }

}
