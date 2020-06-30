package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "strike_off_objections")
public class StrikeOffObjectionsEntity {


    public static class Builder {
        private String companyNumber;
        private String reason;
        private CreatedBy createdBy;
        private LocalDateTime createdOn;
        private ObjectionStatus status;
        private String httpRequestId;

        public Builder withCompanyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder withReason(String reason) {
            this.reason = reason;
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

        public Builder withStatus(ObjectionStatus status) {
            this.status = status;
            return this;
        }

        public Builder withHttpRequestId(String httpRequestId) {
            this.httpRequestId = httpRequestId;
            return this;
        }

        public StrikeOffObjectionsEntity build() {
            StrikeOffObjectionsEntity strikeOffObjectionsEntity = new StrikeOffObjectionsEntity();
            strikeOffObjectionsEntity.setCompanyNumber(this.companyNumber);
            strikeOffObjectionsEntity.setReason(this.reason);
            strikeOffObjectionsEntity.setCreatedBy(this.createdBy);
            strikeOffObjectionsEntity.setCreatedOn(this.createdOn);
            strikeOffObjectionsEntity.setStatus(this.status);
            strikeOffObjectionsEntity.setHttpRequestId(this.httpRequestId);
            return strikeOffObjectionsEntity;
        }
    }

    @Id
    private String id;
    @Field("company_number")
    private String companyNumber;
    @Field("reason")
    private String reason;
    @Field("created_by")
    private CreatedBy createdBy;
    @Field("created_on")
    private LocalDateTime createdOn;
    @Field("status")
    private ObjectionStatus status;
    @Field("http_request_id")
    private String httpRequestId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public void setCompanyNumber(String companyNumber) {
        this.companyNumber = companyNumber;
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

    public ObjectionStatus getStatus() {
        return status;
    }

    public void setStatus(ObjectionStatus status) {
        this.status = status;
    }

    public String getHttpRequestId() {
        return httpRequestId;
    }

    public void setHttpRequestId(String httpRequestId) {
        this.httpRequestId = httpRequestId;
    }
}
