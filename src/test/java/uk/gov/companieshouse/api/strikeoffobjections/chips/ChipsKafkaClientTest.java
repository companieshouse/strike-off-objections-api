package uk.gov.companieshouse.api.strikeoffobjections.chips;

import org.apache.avro.Schema;
import org.apache.commons.lang.StringUtils;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.strikeoffobjections.Application;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.AvroSerializer;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.service.ServiceException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class ChipsKafkaClientTest {

    private static final String REQUEST_ID = "3324324";
    private static final String OBJECTION_ID = "OBJECTION_ID";
    private static final String COMPANY_NUMBER = "12345678";
    private static final List<Attachment> ATTACHMENTS = new ArrayList<>();
    private static final String FULL_NAME = "Joe Bloggs";
    private static final Boolean SHARE_IDENTITY = true;
    private static final String CUSTOMER_EMAIL = "test123@ch.gov.uk";
    private static final String REASON = "This is a test";
    private static final String DOWNLOAD_URL_PREFIX = "http://chs-test-web:4000/strike-off-objections/download";
    private static final String CHIPS_REST_ENDPOINT = "/endpoint";
    private static final String CHIPS_REST_INTERFACES_SEND_TOPIC = "chips-topic";
    private static final LocalDateTime DATE_TIME = LocalDateTime.of(2020, 11, 24, 15, 34);
    private static final Long TIMESTAMP = DATE_TIME.atZone(ZoneId.systemDefault()).toEpochSecond();

    @Mock
    private CHKafkaProducer producer;

    @Mock
    private AvroSerializer avroSerializer;

    @Mock
    private Schema schema;

    @Mock
    private ApiLogger logger;

    @Mock
    private Supplier<LocalDateTime> dateTimeSupplier;

    @Mock
    private Future<RecordMetadata> future;

    @Captor
    private ArgumentCaptor<ChipsRestInterfacesSend> chipsRestInterfacesSendArgumentCaptor;

    @Captor
    private ArgumentCaptor<Message> messageArgumentCaptor;

    @Captor
    private ArgumentCaptor<Map<String, Object>> logMapArgumentCaptor;

    @InjectMocks
    private ChipsKafkaClient chipsKafkaClient;

    @BeforeEach
    void setup() {
        Utils.setTestAttachmentsWithLinks(ATTACHMENTS);
        ReflectionTestUtils.setField(chipsKafkaClient, "chipsRestInterfacesEndpoint", CHIPS_REST_ENDPOINT);
        ReflectionTestUtils.setField(chipsKafkaClient, "chipsRestInterfacesSendTopic", CHIPS_REST_INTERFACES_SEND_TOPIC);

        when(dateTimeSupplier.get()).thenReturn(DATE_TIME);
    }

    @Test
    void testSendToChips() throws ServiceException, IOException, ExecutionException, InterruptedException {
        final byte[] serializedData = new byte[1];
        final ChipsRequest chipsRequest = getChipsRequest();

        when(avroSerializer.serialize(any(ChipsRestInterfacesSend.class))).thenReturn(serializedData);
        when(producer.sendAndReturnFuture(any(Message.class))).thenReturn(future);

        chipsKafkaClient.sendToChips(REQUEST_ID, chipsRequest);

        verify(avroSerializer, times(1)).serialize(chipsRestInterfacesSendArgumentCaptor.capture());

        ChipsRestInterfacesSend chipsRestInterfacesSend = chipsRestInterfacesSendArgumentCaptor.getValue();
        assertEquals(Application.APP_NAMESPACE, chipsRestInterfacesSend.getAppId());
        assertTrue(StringUtils.isNotBlank(chipsRestInterfacesSend.getMessageId()));
        assertEquals(String.valueOf(TIMESTAMP), chipsRestInterfacesSend.getCreatedAt());
        assertEquals(CHIPS_REST_ENDPOINT, chipsRestInterfacesSend.getChipsRestEndpoint());
        assertEquals(0, chipsRestInterfacesSend.getAttempt());

        String chipsData = chipsRestInterfacesSend.getData();
        assertTrue(chipsData.contains(OBJECTION_ID));
        assertTrue(chipsData.contains(COMPANY_NUMBER));
        assertTrue(chipsData.contains(Utils.TEST_ATTACHMENT_1_ID));
        assertTrue(chipsData.contains(Utils.TEST_ATTACHMENT_2_ID));
        assertTrue(chipsData.contains(Utils.TEST_ATTACHMENT_1_URL));
        assertTrue(chipsData.contains(Utils.TEST_ATTACHMENT_2_URL));
        assertTrue(chipsData.contains(OBJECTION_ID));
        assertTrue(chipsData.contains(FULL_NAME));
        assertTrue(chipsData.contains(SHARE_IDENTITY.toString()));
        assertTrue(chipsData.contains(CUSTOMER_EMAIL));
        assertTrue(chipsData.contains(REASON));
        assertTrue(chipsData.contains(DOWNLOAD_URL_PREFIX));

        verify(producer, times(1)).sendAndReturnFuture(messageArgumentCaptor.capture());

        Message message = messageArgumentCaptor.getValue();
        assertEquals(serializedData, message.getValue());
        assertEquals(CHIPS_REST_INTERFACES_SEND_TOPIC, message.getTopic());
        assertEquals(TIMESTAMP, message.getTimestamp());

        verify(future, times(1)).get();
    }

    @Test
    void testSendToChipsLogging() throws ServiceException, IOException {
        final ChipsRequest chipsRequest = getChipsRequest();
        final String sendingLogMessage = "About to send kafka message to Chips Rest Interfaces Consumer";
        final String finishedSendingLogMessage = "Finished sending kafka message to Chips Rest Interfaces Consumer";
        final String topicKey  = "topic";
        final String messageIdKey = "message_id";
        final String messageContentsKey = "message_contents";

        when(producer.sendAndReturnFuture(any(Message.class))).thenReturn(future);

        chipsKafkaClient.sendToChips(REQUEST_ID, chipsRequest);

        verify(avroSerializer, times(1)).serialize(chipsRestInterfacesSendArgumentCaptor.capture());

        ChipsRestInterfacesSend chipsRestInterfacesSend = chipsRestInterfacesSendArgumentCaptor.getValue();

        verify(logger, times(1)).infoContext(eq(REQUEST_ID), eq(sendingLogMessage), logMapArgumentCaptor.capture());

        Map<String, Object> logMap = logMapArgumentCaptor.getValue();
        assertEquals(CHIPS_REST_INTERFACES_SEND_TOPIC, logMap.get(topicKey));
        assertTrue(StringUtils.isNotBlank((String)logMap.get(messageIdKey)));

        verify(logger, times(1)).debugContext(eq(REQUEST_ID), eq(sendingLogMessage), logMapArgumentCaptor.capture());

        logMap = logMapArgumentCaptor.getValue();
        assertEquals(CHIPS_REST_INTERFACES_SEND_TOPIC, logMap.get(topicKey));
        assertTrue(StringUtils.isNotBlank((String)logMap.get(messageIdKey)));
        assertEquals(chipsRestInterfacesSend, logMap.get(messageContentsKey));

        verify(logger, times(1)).infoContext(eq(REQUEST_ID), eq(finishedSendingLogMessage), logMapArgumentCaptor.capture());

        logMap = logMapArgumentCaptor.getValue();
        assertEquals(CHIPS_REST_INTERFACES_SEND_TOPIC, logMap.get(topicKey));
        assertTrue(StringUtils.isNotBlank((String)logMap.get(messageIdKey)));
    }

    @Test
    void testSendToChipsIOExceptionHandling() throws IOException {
        final ChipsRequest chipsRequest = getChipsRequest();
        final IOException ioException = new IOException("error");

        doThrow(ioException).when(avroSerializer).serialize(any(ChipsRestInterfacesSend.class));

        ServiceException serviceException = assertThrows(ServiceException.class,
                () -> chipsKafkaClient.sendToChips(REQUEST_ID, chipsRequest));

        verify(logger, times(1)).errorContext(REQUEST_ID, ioException);
        assertEquals(ioException.getMessage(), serviceException.getMessage());
        assertEquals(ioException, serviceException.getCause());
    }

    @Test
    void testSendToChipsExecutionExceptionHandling() throws ExecutionException, InterruptedException {
        final ChipsRequest chipsRequest = getChipsRequest();
        final ExecutionException executionException = new ExecutionException("error", new Exception());

        when(producer.sendAndReturnFuture(any(Message.class))).thenReturn(future);
        doThrow(executionException).when(future).get();

        ServiceException serviceException = assertThrows(ServiceException.class,
                () -> chipsKafkaClient.sendToChips(REQUEST_ID, chipsRequest));

        verify(logger, times(1)).errorContext(REQUEST_ID, executionException);
        assertEquals(executionException.getMessage(), serviceException.getMessage());
        assertEquals(executionException, serviceException.getCause());
    }

    @Test
    void testSendToChipsInterruptedExceptionHandling() throws ExecutionException, InterruptedException {
        final ChipsRequest chipsRequest = getChipsRequest();
        final InterruptedException interruptedException = new InterruptedException();

        when(producer.sendAndReturnFuture(any(Message.class))).thenReturn(future);
        doThrow(interruptedException).when(future).get();

        ServiceException serviceException = assertThrows(ServiceException.class,
                () -> chipsKafkaClient.sendToChips(REQUEST_ID, chipsRequest));

        assertTrue(Thread.currentThread().isInterrupted());
        verify(logger, times(1)).errorContext(REQUEST_ID, interruptedException);
        assertTrue(serviceException.getMessage().contains("Thread Interrupted"));
        assertEquals(interruptedException, serviceException.getCause());
    }

    private ChipsRequest getChipsRequest() {
        return new ChipsRequest(
                OBJECTION_ID,
                COMPANY_NUMBER,
                ATTACHMENTS,
                OBJECTION_ID,
                FULL_NAME,
                SHARE_IDENTITY,
                CUSTOMER_EMAIL,
                REASON,
                DOWNLOAD_URL_PREFIX
        );
    }
}
