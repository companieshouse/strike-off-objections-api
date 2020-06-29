package uk.gov.companieshouse.api.strikeoffobjections.model.request;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "strike_off_objections_requests")
public class StrikeOffObjectionsRequestEntity {


    public static class Builder {
        private String requestId;
        private String username;
        private String eMail;
        private String requestInformation;
        private LocalDateTime createdOn;
        private RequestStatus status;

        public Builder withRequestId(String requestId) {
            this.requestId = requestId;
            return this;
        }

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withEMail(String eMail) {
            this.eMail = eMail;
            return this;
        }

        public Builder withRequestInformation(String requestInformation) {
            this.requestInformation = requestInformation;
            return this;
        }

        public Builder withCreatedOn(LocalDateTime createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public Builder withStatus(RequestStatus status) {
            this.status = status;
            return this;
        }

        public StrikeOffObjectionsRequestEntity build() {
            StrikeOffObjectionsRequestEntity strikeOffObjectionsRequestEntity = new StrikeOffObjectionsRequestEntity();
            strikeOffObjectionsRequestEntity.setRequestId(this.requestId);
            strikeOffObjectionsRequestEntity.setUsername(this.username);
            strikeOffObjectionsRequestEntity.setEMail(this.eMail);
            strikeOffObjectionsRequestEntity.setRequestInformation(this.requestInformation);
            strikeOffObjectionsRequestEntity.setCreatedOn(this.createdOn);
            strikeOffObjectionsRequestEntity.setStatus(this.status);
            return strikeOffObjectionsRequestEntity;
        }
    }

    @Id
    private String id;
    @Field("request_id")
    private String requestId;
    @Field("username")
    private String username;
    @Field("e_mail")
    private String eMail;
    @Field("reason_information")
    private String requestInformation;
    @Field("created_on")
    private LocalDateTime createdOn;
    @Field("status")
    private RequestStatus status;

    public String getId() {
        return id;
    }

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEMail() {
        return eMail;
    }

    public void setEMail(String eMail) {
        this.eMail = eMail;
    }

    public String getRequestInformation() {
        return requestInformation;
    }

    public void setRequestInformation(String requestInformation) {
        this.requestInformation = requestInformation;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public RequestStatus getStatus() {
        return status;
    }

    public void setStatus(RequestStatus status) {
        this.status = status;
    }
}
