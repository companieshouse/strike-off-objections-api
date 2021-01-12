package uk.gov.companieshouse.api.strikeoffobjections.chips;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericRecord;
import org.apache.kafka.clients.producer.RecordMetadata;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.Application;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.AvroSerializer;
import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;
import uk.gov.companieshouse.kafka.message.Message;
import uk.gov.companieshouse.kafka.producer.CHKafkaProducer;
import uk.gov.companieshouse.service.ServiceException;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.function.Supplier;

@Component
@Profile("chips-kafka")
public class ChipsKafkaClient implements ChipsSender {

    @Autowired
    private CHKafkaProducer producer;

    @Autowired
    private AvroSerializer avroSerializer;

    @Autowired
    @Qualifier("chips-rest-interfaces-send")
    private Schema schema;

    @Value("${OBJECT_TO_STRIKE_OFF_CHIPS_REST_ENDPOINT}")
    private String chipsRestEndpoint;

    @Autowired
    private ApiLogger logger;

    @Autowired
    private Supplier<LocalDateTime> dateTimeSupplier;

    @Override
    public void sendToChips(String requestId, ChipsRequest chipsRequest) throws ServiceException {
        try {
            Long timestamp = dateTimeSupplier.get().atZone(ZoneId.systemDefault()).toEpochSecond();
            GenericRecord genericRecord = getGenericRecord(chipsRequest, timestamp);

            logger.infoContext(requestId, "About to send to chips via kafka");

            Message message = new Message();
            byte[] serializedData = avroSerializer.serialize(genericRecord, schema);
            message.setValue(serializedData);
            message.setTopic("chips-rest-interfaces-send");
            message.setTimestamp(timestamp);
            Future<RecordMetadata> future = producer.sendAndReturnFuture(message);
            future.get();
        } catch (IOException | ExecutionException e) {
            logger.errorContext(requestId, e);
            throw new ServiceException(e.getMessage());
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            logger.errorContext(requestId, ie);
            throw new ServiceException("Thread Interrupted when Chips future was sent and returned");
        }
    }

    private GenericRecord getGenericRecord(ChipsRequest chipsRequest, Long timestamp) throws JsonProcessingException {
        return new GenericRecordBuilder(schema)
                .withSourceAppId(Application.APP_NAMESPACE)
                .withMessageId(UUID.randomUUID().toString())
                .withData(convertToJSON(chipsRequest))
                .withCreatedAtTimestampInSeconds(timestamp)
                .withChipsRestEndpoint(chipsRestEndpoint)
                .withAttemptNumber(1)
                .build();
    }

    private String convertToJSON(ChipsRequest chipsRequest) throws JsonProcessingException {
        ObjectWriter ow = new ObjectMapper().writer().withDefaultPrettyPrinter();
        return ow.writeValueAsString(chipsRequest);
    }
}
