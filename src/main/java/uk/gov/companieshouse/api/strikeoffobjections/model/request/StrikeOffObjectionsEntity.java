package uk.gov.companieshouse.api.strikeoffobjections.model.request;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "strike_off_objections")
public class StrikeOffObjectionsEntity {


    public static class Builder {
        private String username;
        private String eMailAddress;
        private String reason;
        private CreatedBy createdBy;
        private LocalDateTime createdOn;
        private RequestStatus status;

        public Builder withUsername(String username) {
            this.username = username;
            return this;
        }

        public Builder withEMailAddress(String eMail) {
            this.eMailAddress = eMail;
            return this;
        }

        public Builder withReason(String requestInformation) {
            this.reason = requestInformation;
            return this;
        }

        public Builder withCreatedBy(CreatedBy createdBy) {
            this.createdBy = createdBy;
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

        public StrikeOffObjectionsEntity build() {
            StrikeOffObjectionsEntity strikeOffObjectionsEntity = new StrikeOffObjectionsEntity();
            strikeOffObjectionsEntity.setUsername(this.username);
            strikeOffObjectionsEntity.setEMailAddress(this.eMailAddress);
            strikeOffObjectionsEntity.setReason(this.reason);
            strikeOffObjectionsEntity.setCreatedBy(this.createdBy);
            strikeOffObjectionsEntity.setCreatedOn(this.createdOn);
            strikeOffObjectionsEntity.setStatus(this.status);
            return strikeOffObjectionsEntity;
        }
    }

    @Id
    private String id;
    @Field("username")
    private String username;
    @Field("e_mail_address")
    private String eMailAddress;
    @Field("reason")
    private String reason;
    @Field("created_by")
    private CreatedBy createdBy;
    @Field("created_on")
    private LocalDateTime createdOn;
    @Field("status")
    private RequestStatus status;

    public String getId() {
        return id;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEMailAddress() {
        return eMailAddress;
    }

    public void setEMailAddress(String eMailAddress) {
        this.eMailAddress = eMailAddress;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    public CreatedBy getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(CreatedBy createdBy) {
        this.createdBy = createdBy;
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
