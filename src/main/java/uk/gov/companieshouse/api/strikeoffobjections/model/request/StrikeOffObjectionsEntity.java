package uk.gov.companieshouse.api.strikeoffobjections.model.request;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "strike_off_objections")
public class StrikeOffObjectionsEntity {


    public static class Builder {
        private String email;
        private String reason;
        private CreatedBy createdBy;
        private LocalDateTime createdOn;
        private RequestStatus status;

        public Builder withEmail(String email) {
            this.email = email;
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
            strikeOffObjectionsEntity.setEmail(this.email);
            strikeOffObjectionsEntity.setReason(this.reason);
            strikeOffObjectionsEntity.setCreatedBy(this.createdBy);
            strikeOffObjectionsEntity.setCreatedOn(this.createdOn);
            strikeOffObjectionsEntity.setStatus(this.status);
            return strikeOffObjectionsEntity;
        }
    }

    @Id
    private String id;
    @Field("email")
    private String email;
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

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
