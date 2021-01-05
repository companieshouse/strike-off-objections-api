package uk.gov.companieshouse.api.strikeoffobjections.model.chipsRestInterfacesKafkaMessage;

import java.util.Map;

public class ChipsRestInterfacesKafkaMessageContent {

    private Map<String, Object> appId;
    private Map<String, Object> messageId;
    private Map<String, Object> data;
    private Map<String, Object> chipsRestEndpoint;
    private Map<String, Object> createdAt;

    public Map<String, Object> getAppId() {
        return appId;
    }

    public void setAppId(Map<String, Object> appId) {
        this.appId = appId;
    }

    public Map<String, Object> getMessageId() {
        return messageId;
    }

    public void setMessageId(Map<String, Object> messageId) {
        this.messageId = messageId;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public void setData(Map<String, Object> data) {
        this.data = data;
    }

    public Map<String, Object> getChipsRestEndpoint() {
        return chipsRestEndpoint;
    }

    public void setChipsRestEndpoint(Map<String, Object> chipsRestEndpoint) {
        this.chipsRestEndpoint = chipsRestEndpoint;
    }

    public Map<String, Object> getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Map<String, Object> createdAt) {
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
