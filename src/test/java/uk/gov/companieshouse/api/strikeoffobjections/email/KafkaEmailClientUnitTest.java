package uk.gov.companieshouse.api.strikeoffobjections.email;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import org.apache.avro.Schema;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.AvroSerializer;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.email.EmailContent;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.service.ServiceException;

@Unit
@ExtendWith(MockitoExtension.class)
class KafkaEmailClientUnitTest {

    private static final LocalDateTime CREATED_AT = LocalDateTime.of(2019, 1, 1, 0, 0);
    private static final Future MOCKED_FUTURE = Mockito.mock(Future.class);
    private static final Future FAULTY_MOCKED_FUTURE = Mockito.mock(Future.class);

    private static final String MESSAGE_ID = "abc";
    private static final String NO_LONGER_REQUIRED_TEMPLATE_MESSAGE_TYPE =
            "promise_to_file_no_longer_required";
    private static final String EMAIL_NO_LONGER_REQUIRED_TEMPLATE_APP_ID =
            "filing_processed_notification_sender.promise_to_file_no_longer_required";
    private static final String CUSTOMER_EMAIL = "example@test.co.uk";

    private KafkaEmailClient kafkaEmailClient;
    private Schema testSchema;
    private EmailContent emailContent;

    @Mock
    private CHKafkaProducer producer;

    @Mock
    private AvroSerializer avroSerializer;

    @Mock
    private AvroSerializer faultyAvroSerializer;

    @BeforeEach
    void setup() throws IOException {
        emailContent = Utils.buildEmailContent(
                EMAIL_NO_LONGER_REQUIRED_TEMPLATE_APP_ID,
                MESSAGE_ID,
                NO_LONGER_REQUIRED_TEMPLATE_MESSAGE_TYPE,
                Utils.getDummyEmailData(),
                CUSTOMER_EMAIL,
                CREATED_AT);

        testSchema =
                Utils.getDummySchema(this.getClass().getClassLoader().getResource("email/email-send.avsc"));
    }

    @Test
    void checkFutureIsCalledWhenSendingEmailToKafka()
            throws ServiceException, ExecutionException, InterruptedException {
        when(producer.sendAndReturnFuture(any())).thenReturn(MOCKED_FUTURE);
        kafkaEmailClient = new KafkaEmailClient(producer, avroSerializer, testSchema);
        kafkaEmailClient.sendEmailToKafka(emailContent);
        verify(MOCKED_FUTURE, times(1)).get();
    }

    @Test
    void checkServiceExceptionIsThrownWhenSerializerThrowsIOExcpetion() throws IOException {
        doThrow(IOException.class).when(faultyAvroSerializer).serialize(emailContent, testSchema);
        kafkaEmailClient = new KafkaEmailClient(producer, faultyAvroSerializer, testSchema);
        assertThrows(ServiceException.class, () -> kafkaEmailClient.sendEmailToKafka(emailContent));
    }

    @Test
    void checkServiceExceptionIsThrownWhenFutureThrowsExecutionException()
            throws ExecutionException, InterruptedException {
        when(producer.sendAndReturnFuture(any())).thenReturn(FAULTY_MOCKED_FUTURE);
        kafkaEmailClient = new KafkaEmailClient(producer, avroSerializer, testSchema);
        doThrow(ExecutionException.class).when(FAULTY_MOCKED_FUTURE).get();
        assertThrows(ServiceException.class, () -> kafkaEmailClient.sendEmailToKafka(emailContent));
    }
}
