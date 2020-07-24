package uk.gov.companieshouse.api.strikeoffobjections.model.model;

import java.util.Map;

public class Email {

    public static final class Builder {
        private String appId;
        private String messageId;
        private String messageType;
        private Map<String, Object> data;
        private String emailAddress;
        private String createdAt;

        public Builder() {
        }

        public Builder appId(String val) {
            appId = val;
            return this;
        }

        public Builder messageId(String val) {
            messageId = val;
            return this;
        }

        public Builder messageType(String val) {
            messageType = val;
            return this;
        }

        public Builder data(Map<String, Object> val) {
            data = val;
            return this;
        }

        public Builder emailAddress(String val) {
            emailAddress = val;
            return this;
        }

        public Builder createdAt(String val) {
            createdAt = val;
            return this;
        }

        public Email build() {
            return new Email(this);
        }
    }
    
    private final String appId;
    private final String messageId;
    private final String messageType;
    private final Map<String, Object> data;
    private final String emailAddress;
    private final String createdAt;

    private Email(Builder builder) {
        this.appId = builder.appId;
        this.messageId = builder.messageId;
        this.messageType = builder.messageType;
        this.data = builder.data;
        this.emailAddress = builder.emailAddress;
        this.createdAt = builder.createdAt;
    }

    public String getAppId() {
        return appId;
    }

    public String getMessageId() {
        return messageId;
    }

    public String getMessageType() {
        return messageType;
    }

    public Map<String, Object> getData() {
        return data;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getCreatedAt() {
        return createdAt;
    }
}
