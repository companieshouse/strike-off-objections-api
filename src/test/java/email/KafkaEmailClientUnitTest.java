package email;

import org.apache.avro.Schema;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.FormatUtils;
import uk.gov.companieshouse.api.strikeoffobjections.email.AvroSerializer;
import uk.gov.companieshouse.api.strikeoffobjections.email.KafkaEmailClient;
import uk.gov.companieshouse.api.strikeoffobjections.model.model.EmailContent;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.service.ServiceException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class KafkaEmailClientUnitTest {

    private static final LocalDateTime CREATED_AT =
            LocalDateTime.of(2019, 1, 1, 0, 0);
    private static final Future<RecordMetadata> mockedFuture = Mockito.mock(Future.class);
    private static final Future<RecordMetadata> faultyMmockedFuture = Mockito.mock(Future.class);

    private static final String MESSAGE_ID = "abc";
    private static final String NO_LONGER_REQUIRED_TEMPLATE_MESSAGE_TYPE = "promise_to_file_no_longer_required";
    private static final String EMAIL_NO_LONGER_REQUIRED_TEMPLATE_APP_ID = "filing_processed_notification_sender.promise_to_file_no_longer_required";
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
    public void setup() throws IOException {
        emailContent = Utils.buildEmailDocument(
                EMAIL_NO_LONGER_REQUIRED_TEMPLATE_APP_ID,
                MESSAGE_ID,
                NO_LONGER_REQUIRED_TEMPLATE_MESSAGE_TYPE,
                Utils.getDummyEmailData(),
                CUSTOMER_EMAIL,
                FormatUtils.formatTimestamp(CREATED_AT));

        testSchema = Utils.getDummySchema(this.getClass().getClassLoader().getResource(
                "email/email-send.avsc"));
    }

    @Test
    public void checkFutureIsCalledWhenSendingEmailToKafka()
            throws ServiceException, ExecutionException, IOException, InterruptedException {
        when(producer.sendAndReturnFuture(any())).thenReturn(mockedFuture);
        kafkaEmailClient = new KafkaEmailClient(producer,
                avroSerializer, testSchema);
        kafkaEmailClient.sendEmailToKafka(CREATED_AT, emailContent);
        verify(mockedFuture, times(1)).get();
    }

    @Test
    public void checkServiceExcpetionIsThrownWhenSerializerThrowsIOExcpetion()
            throws IOException {
        doThrow(IOException.class).when(faultyAvroSerializer).serialize(emailContent, testSchema);
        kafkaEmailClient = new KafkaEmailClient(producer,
                faultyAvroSerializer, testSchema);
        assertThrows(ServiceException.class, () -> {
            kafkaEmailClient.sendEmailToKafka(CREATED_AT, emailContent);
        });
    }

    @Test
    public void checkServiceExcpetionIsThrownWhenFutureThrowsExecutionException()
            throws ExecutionException, InterruptedException {
        when(producer.sendAndReturnFuture(any())).thenReturn(faultyMmockedFuture);
        kafkaEmailClient = new KafkaEmailClient(producer,
                avroSerializer, testSchema);
        doThrow(ExecutionException.class).when(faultyMmockedFuture).get();
        assertThrows(ServiceException.class, () -> {
            kafkaEmailClient.sendEmailToKafka(CREATED_AT, emailContent);
        });
    }
}

