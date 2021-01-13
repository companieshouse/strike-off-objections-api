package uk.gov.companieshouse.api.strikeoffobjections.common;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.DatumWriter;
import org.apache.avro.io.Encoder;
import org.apache.avro.io.EncoderFactory;
import org.apache.avro.specific.SpecificDatumWriter;
import org.apache.avro.specific.SpecificRecord;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.model.email.EmailContent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class AvroSerializer {

    private final ObjectMapper objectMapper = new ObjectMapper();

    public byte[] serialize(GenericRecord genericRecord, Schema schema) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(stream, null);
        GenericDatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        datumWriter.write(genericRecord, encoder);
        encoder.flush();
        return stream.toByteArray();
    }

    public byte[] serialize(SpecificRecord data) throws IOException {
        DatumWriter<SpecificRecord> datumWriter = new SpecificDatumWriter<>();

        try(ByteArrayOutputStream out = new ByteArrayOutputStream()) {
            Encoder encoder = EncoderFactory.get().binaryEncoder(out, null);
            datumWriter.setSchema(data.getSchema());
            datumWriter.write(data, encoder);
            encoder.flush();

            byte[] serializedData = out.toByteArray();
            encoder.flush();

            return serializedData;
        }
    }

    public GenericRecord buildAvroGenericRecord(EmailContent emailContent, Schema schema)
            throws JsonProcessingException {

        GenericRecord documentData = new GenericData.Record(schema);
        documentData.put("app_id", emailContent.getOriginatingAppId());
        documentData.put("message_id", emailContent.getMessageId());
        documentData.put("message_type", emailContent.getMessageType());
        documentData.put("data", objectMapper.writeValueAsString(emailContent.getData()));
        documentData.put("email_address", emailContent.getEmailAddress());
        documentData.put("created_at", FormatUtils.formatTimestamp(emailContent.getCreatedAt()));
        return documentData;
    }
}
