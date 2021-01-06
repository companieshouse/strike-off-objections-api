package uk.gov.companieshouse.api.strikeoffobjections.model.chipsrestinterfacesconsumer;

import java.time.LocalDateTime;
import java.util.Map;

public class ChipsRestInterfacesConsumerContent {

    public static final class Builder {
        private String appId;
        private String messageId;
        private Map<String, Object> data;
        private String chipsRestEndpoint;
        private LocalDateTime createdAt;

        public Builder withAppId(String val) {
            appId = val;
            return this;
        }

        public Builder withMessageId(String val) {
            messageId = val;
            return this;
        }

        public Builder withData(Map<String, Object> val) {
            data = val;
            return this;
        }

        public Builder withChipsRestEndpoint(String val) {
            chipsRestEndpoint = val;
            return this;
        }

        public Builder withCreatedAt(LocalDateTime val) {
            createdAt = val;
            return this;
        }

        public ChipsRestInterfacesConsumerContent build() {
            return new ChipsRestInterfacesConsumerContent(this);
        }
    }

    private final String appId;
    private final String messageId;
    private final Map<String, Object> data;
    private final String chipsRestEndpoint;
    private final LocalDateTime createdAt;

    private ChipsRestInterfacesConsumerContent(ChipsRestInterfacesConsumerContent.Builder builder) {
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
