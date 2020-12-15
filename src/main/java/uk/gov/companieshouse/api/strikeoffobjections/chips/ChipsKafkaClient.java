package uk.gov.companieshouse.api.strikeoffobjections.chips;

import org.apache.avro.Schema;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.email.AvroSerializer;
import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;
import uk.gov.companieshouse.api.strikeoffobjections.model.email.EmailContent;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
@Profile("kafka")
public class ChipsKafkaClient implements ChipsSender {

    @Autowired
    private CHKafkaProducer producer;
    @Autowired
    private AvroSerializer avroSerializer;
    @Autowired
    @Qualifier("chips-kafka-send")
    private Schema schema;

    @Autowired
    ApiLogger logger;

    @Override
    public void sendToChips(String requestId, ChipsRequest chipsRequest) {
        Map<String, Object> map = new HashMap<>();
        map.put("Hello there", "testing");
        EmailContent emailContent = new EmailContent.Builder().withData(map).withCreatedAt(LocalDateTime.now())
                .withMessageId("MESSAGE_ID")
                .withEmailAddress("EMAIL_ADDRESS")
                .withMessageType("MESSAGE_TYPE")
                .withOriginatingAppId("strike-off-api")
                .build();
        logger.infoContext(requestId, "About to send to chips via kafka");
        try {
            Message message = new Message();
            byte[] serializedData = avroSerializer.serialize(emailContent, schema);
            message.setValue(serializedData);
            message.setTopic("chips-rest-interfaces-send");
            message.setTimestamp(emailContent.getCreatedAt().atZone(ZoneId.systemDefault()).toEpochSecond());
            Future<RecordMetadata> future = producer.sendAndReturnFuture(message);
            future.get();
        } catch (IOException | ExecutionException e) {
            logger.errorContext(requestId, e);
            return;
            //throw new ServiceException(e.getMessage());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.errorContext(requestId, ie);
            return;
            //throw new ServiceException("Thread Interrupted when future was sent and returned");
        }
    }
}
