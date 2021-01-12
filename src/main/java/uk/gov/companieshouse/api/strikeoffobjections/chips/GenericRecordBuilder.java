package uk.gov.companieshouse.api.strikeoffobjections.chips;

import org.apache.avro.Schema;
import org.apache.avro.generic.GenericData;
import org.apache.avro.generic.GenericRecord;

public class GenericRecordBuilder {

    private Schema schema;
    private String sourceAppId;
    private String messageId;
    private String data;
    private Long createdAtTimestampInSeconds;
    private String chipsRestEndpoint;
    private int attemptNumber;

    GenericRecordBuilder(Schema schema) {
        this.schema = schema;
    }

    GenericRecordBuilder withSourceAppId(String appId) {
        this.sourceAppId = appId;
        return this;
    }

    GenericRecordBuilder withMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    GenericRecordBuilder withData(String data) {
        this.data = data;
        return this;
    }

    GenericRecordBuilder withCreatedAtTimestampInSeconds(Long createdAtTimestampInSeconds) {
        this.createdAtTimestampInSeconds = createdAtTimestampInSeconds;
        return this;
    }

    GenericRecordBuilder withChipsRestEndpoint(String chipsRestEndpoint) {
        this.chipsRestEndpoint = chipsRestEndpoint;
        return this;
    }

    GenericRecordBuilder withAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
        return this;
    }

    public GenericRecord build() {
        GenericRecord genericRecord = new GenericData.Record(schema);
        genericRecord.put("app_id", sourceAppId);
        genericRecord.put("message_id", messageId);
        genericRecord.put("data", data);
        genericRecord.put("created_at", createdAtTimestampInSeconds.toString());
        genericRecord.put("chips_rest_endpoint", chipsRestEndpoint);
        genericRecord.put("attempt", attemptNumber);
        return genericRecord;
    }
}
