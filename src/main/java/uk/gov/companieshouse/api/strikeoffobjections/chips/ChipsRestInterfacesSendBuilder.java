package uk.gov.companieshouse.api.strikeoffobjections.chips;

import uk.gov.companieshouse.chips.ChipsRestInterfacesSend;

public class ChipsRestInterfacesSendBuilder {

    private String sourceAppId;
    private String messageId;
    private String data;
    private String createdAtTimestampInSeconds;
    private String chipsRestEndpoint;
    private int attemptNumber;

    ChipsRestInterfacesSendBuilder withSourceAppId(String appId) {
        this.sourceAppId = appId;
        return this;
    }

    ChipsRestInterfacesSendBuilder withMessageId(String messageId) {
        this.messageId = messageId;
        return this;
    }

    ChipsRestInterfacesSendBuilder withData(String data) {
        this.data = data;
        return this;
    }

    ChipsRestInterfacesSendBuilder withCreatedAtTimestampInSeconds(
            String createdAtTimestampInSeconds) {
        this.createdAtTimestampInSeconds = createdAtTimestampInSeconds;
        return this;
    }

    ChipsRestInterfacesSendBuilder withChipsRestEndpoint(String chipsRestEndpoint) {
        this.chipsRestEndpoint = chipsRestEndpoint;
        return this;
    }

    ChipsRestInterfacesSendBuilder withAttemptNumber(int attemptNumber) {
        this.attemptNumber = attemptNumber;
        return this;
    }

    public ChipsRestInterfacesSend build() {
        ChipsRestInterfacesSend chipsRestInterfacesSend = new ChipsRestInterfacesSend();
        chipsRestInterfacesSend.setAppId(sourceAppId);
        chipsRestInterfacesSend.setMessageId(messageId);
        chipsRestInterfacesSend.setData(data);
        chipsRestInterfacesSend.setCreatedAt(createdAtTimestampInSeconds);
        chipsRestInterfacesSend.setChipsRestEndpoint(chipsRestEndpoint);
        chipsRestInterfacesSend.setAttempt(attemptNumber);
        return chipsRestInterfacesSend;
    }
}
