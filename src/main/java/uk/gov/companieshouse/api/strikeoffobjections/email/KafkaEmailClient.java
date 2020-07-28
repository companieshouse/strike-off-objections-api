package uk.gov.companieshouse.api.strikeoffobjections.email;

import org.apache.avro.Schema;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.model.model.EmailContent;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.service.ServiceException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Component
public class KafkaEmailClient {

    @Value("${EMAIL_SEND_QUEUE_TOPIC}")
    private String emailSendQueueTopic;

    private CHKafkaProducer producer;
    private AvroSerializer avroSerializer;
    private Schema schema;

    @Autowired
    public KafkaEmailClient(CHKafkaProducer producer,
                            AvroSerializer avroSerializer,
                            Schema schema) {
        this.producer = producer;
        this.avroSerializer = avroSerializer;
        this.schema = schema;
    }

    public void sendEmailToKafka(EmailContent emailContent)
            throws ServiceException {
        try {
            Message message = new Message();
            byte[] serializedData = avroSerializer.serialize(emailContent, schema);
            message.setValue(serializedData);
            message.setTopic(emailSendQueueTopic);
            message.setTimestamp(emailContent.getCreatedAt().atZone(ZoneId.systemDefault()).toEpochSecond());
            Future<RecordMetadata> future = producer.sendAndReturnFuture(message);
            future.get();
        } catch (IOException | ExecutionException e) {
            throw new ServiceException(e.getMessage());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new ServiceException("Thread Interrupted when future was sent and returned");
        }
    }

}
