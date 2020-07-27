package uk.gov.companieshouse.api.strikeoffobjections.email;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericDatumWriter;
import org.apache.avro.generic.GenericRecord;
import org.apache.avro.io.BinaryEncoder;
import org.apache.avro.io.EncoderFactory;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.model.model.EmailContent;

import java.io.ByteArrayOutputStream;
import java.io.IOException;

@Component
public class AvroSerializer {

    public byte[] serialize(EmailContent emailContent, Schema schema) throws IOException {
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        BinaryEncoder encoder = EncoderFactory.get().binaryEncoder(stream, null);
        GenericDatumWriter<GenericRecord> datumWriter = new GenericDatumWriter<>(schema);
        datumWriter.write(buildAvroGenericRecord(emailContent, schema), encoder);
        encoder.flush();
        return stream.toByteArray();
    }

    private GenericRecord buildAvroGenericRecord(EmailContent emailContent, Schema schema) {
        GenericRecord documentData = new GenericData.Record(schema);
        documentData.put("app_id", emailContent.getOriginatingAppId());
        documentData.put("message_id", emailContent.getMessageId());
        documentData.put("message_type", emailContent.getMessageType());
        documentData.put("data", emailContent.getData());
        documentData.put("email_address", emailContent.getEmailAddress());
        documentData.put("created_at", emailContent.getCreatedAt());
        return documentData;
    }
}
