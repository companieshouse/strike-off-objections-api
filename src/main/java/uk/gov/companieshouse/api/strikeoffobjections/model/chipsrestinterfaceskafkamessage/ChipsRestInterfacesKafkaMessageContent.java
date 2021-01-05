package uk.gov.companieshouse.api.strikeoffobjections.model.chipsrestinterfaceskafkamessage;

import java.time.LocalDateTime;
import java.util.Map;

public class ChipsRestInterfacesKafkaMessageContent {

    private String appId;
    private String messageId;
    private Map<String, Object> data;
    private String chipsRestEndpoint;
    private LocalDateTime createdAt;

    public String getAppId() {
        return appId;
    }

    public void setAppId(String appId) {
        this.appId = appId;
    }

    public String getMessageId() {
        return messageId;
    }

    public void setMessageId(String messageId) {
        this.messageId = messageId;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public String getChipsRestEndpoint() {
        return chipsRestEndpoint;
    }

    public void setChipsRestEndpoint(String chipsRestEndpoint) {
        this.chipsRestEndpoint = chipsRestEndpoint;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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
