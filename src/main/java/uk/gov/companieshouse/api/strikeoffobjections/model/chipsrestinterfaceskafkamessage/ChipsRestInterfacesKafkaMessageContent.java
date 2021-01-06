package uk.gov.companieshouse.api.strikeoffobjections.model.chipsrestinterfaceskafkamessage;

import java.time.LocalDateTime;
import java.util.Map;

public class ChipsRestInterfacesKafkaMessageContent {

    public static final class Builder {
        private String appId;
        private String messageId;
        private Map<String, Object> data;
        private String chipsRestEndpoint;
        private LocalDateTime createdAt;

        public ChipsRestInterfacesKafkaMessageContent.Builder withAppId(String val) {
            appId = val;
            return this;
        }

        public ChipsRestInterfacesKafkaMessageContent.Builder withMessageId(String val) {
            messageId = val;
            return this;
        }

        public ChipsRestInterfacesKafkaMessageContent.Builder withData(Map<String, Object> val) {
            data = val;
            return this;
        }

        public ChipsRestInterfacesKafkaMessageContent.Builder withChipsRestEndpoint(String val) {
            chipsRestEndpoint = val;
            return this;
        }

        public ChipsRestInterfacesKafkaMessageContent.Builder withCreatedAt(LocalDateTime val) {
            createdAt = val;
            return this;
        }

        public ChipsRestInterfacesKafkaMessageContent build() {
            return new ChipsRestInterfacesKafkaMessageContent(this);
        }
    }

    private final String appId;
    private final String messageId;
    private final Map<String, Object> data;
    private final String chipsRestEndpoint;
    private final LocalDateTime createdAt;

    private ChipsRestInterfacesKafkaMessageContent(ChipsRestInterfacesKafkaMessageContent.Builder builder) {
        this.appId = builder.appId;
        this.messageId = builder.messageId;
        this.data = builder.data;
        this.chipsRestEndpoint = builder.chipsRestEndpoint;
        this.createdAt = builder.createdAt;
    }

    public String getAppId() {
        return appId;
    }

    public String getMessageId() {
        return messageId;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getChipsRestEndpoint() {
        return chipsRestEndpoint;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    @Override
    public String toString() {
        return "ChipsKafkaMessage{" +
                "appId='" + appId + '\'' +
                ", messageId='" + messageId + '\'' +
                ", data='" + data + '\'' +
                ", chipsRestEndpoint='" + chipsRestEndpoint + '\'' +
                ", createdAt='" + createdAt + '\'' +
                '}';
    }
}
