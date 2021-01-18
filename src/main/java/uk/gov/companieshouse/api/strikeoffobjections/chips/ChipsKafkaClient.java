package uk.gov.companieshouse.api.strikeoffobjections.chips;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.avro.Schema;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.Application;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.AvroSerializer;
import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;
import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.service.ServiceException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

@Component
public class ChipsKafkaClient implements ChipsSender {

    @Autowired
    private CHKafkaProducer producer;

    @Autowired
    private AvroSerializer avroSerializer;

    @Autowired
    @Qualifier("chips-rest-interfaces-send")
    private Schema schema;

    @Value("${CHIPS_REST_INTERFACES_SEND_TOPIC}")
    private String chipsRestInterfacesSendTopic;

    @Value("${OBJECT_TO_STRIKE_OFF_CHIPS_REST_INTERFACES_ENDPOINT}")
    private String chipsRestInterfacesEndpoint;

    @Autowired
    private ApiLogger logger;

    @Autowired
    private Supplier<LocalDateTime> dateTimeSupplier;

    @Override
    public void sendToChips(String requestId, ChipsRequest chipsRequest) throws ServiceException {
        try {
            Long timestamp = dateTimeSupplier.get().atZone(ZoneId.systemDefault()).toEpochSecond();
            ChipsRestInterfacesSend chipsRestInterfacesSend = getChipsRestInterfacesSend(chipsRequest, timestamp);

            Map<String, Object> dataForInfoLogMessage = new HashMap<>();
            dataForInfoLogMessage.put("topic", chipsRestInterfacesSendTopic);
            dataForInfoLogMessage.put("message_id", chipsRestInterfacesSend.getMessageId());

            String logMessageSendText = "About to send kafka message to Chips Rest Interfaces Consumer";
            logger.infoContext(requestId, logMessageSendText, dataForInfoLogMessage);

            Map<String, Object> dataForDebugLogMessage = new HashMap<>(dataForInfoLogMessage);
            dataForDebugLogMessage.put("message_contents", chipsRestInterfacesSend);
            logger.debugContext(requestId, logMessageSendText, dataForDebugLogMessage);

            Message message = new Message();
            byte[] serializedData = avroSerializer.serialize(chipsRestInterfacesSend);
            message.setValue(serializedData);
            message.setTopic(chipsRestInterfacesSendTopic);
            message.setTimestamp(timestamp);

            Future<RecordMetadata> future = producer.sendAndReturnFuture(message);
            future.get();

            logger.infoContext(requestId,
                    "Finished sending kafka message to Chips Rest Interfaces Consumer",
                    dataForInfoLogMessage);
        } catch (IOException | ExecutionException e) {
            logger.errorContext(requestId, e);
            throw new ServiceException(e.getMessage(), e);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.errorContext(requestId, ie);
            throw new ServiceException("Thread Interrupted when ChipsRestInterfacesConsumer future was sent and returned", ie);
        }
    }

    private ChipsRestInterfacesSend getChipsRestInterfacesSend(ChipsRequest chipsRequest, Long timestamp) throws JsonProcessingException {
        return new ChipsRestInterfacesSendBuilder()
                .withSourceAppId(Application.APP_NAMESPACE)
                .withMessageId(UUID.randomUUID().toString())
                .withData(convertToJSON(chipsRequest))
                .withCreatedAtTimestampInSeconds(timestamp.toString())
                .withChipsRestEndpoint(chipsRestInterfacesEndpoint)
                .withAttemptNumber(0)
                .build();
    }

    private String convertToJSON(ChipsRequest chipsRequest) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(chipsRequest);
    }
}
