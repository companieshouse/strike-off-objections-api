package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "strike_off_objections")
public class StrikeOffObjectionsEntity {


    public static class Builder {
        private LocalDateTime createdOn;
        private CreatedBy createdBy;
        private String companyNumber;
        private String reason;
        private ObjectionStatus status;

        public Builder withCreatedOn(LocalDateTime createdOn) {
            this.createdOn = createdOn;
            return this;
        }

        public Builder withCreatedBy(CreatedBy createdBy) {
            this.createdBy = createdBy;
            return this;
        }

        public Builder withCompanyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder withReason(String reason) {
            this.reason = reason;
            return this;
        }


        public Builder withStatus(ObjectionStatus status) {
            this.status = status;
            return this;
        }

        public StrikeOffObjectionsEntity build() {
            StrikeOffObjectionsEntity strikeOffObjectionsEntity = new StrikeOffObjectionsEntity();
            strikeOffObjectionsEntity.setCreatedOn(this.createdOn);
            strikeOffObjectionsEntity.setCreatedBy(this.createdBy);
            strikeOffObjectionsEntity.setCompanyNumber(this.companyNumber);
            strikeOffObjectionsEntity.setReason(this.reason);
            strikeOffObjectionsEntity.setStatus(this.status);
            return strikeOffObjectionsEntity;
        }
    }

    @Id
    private String id;
    @Field("created_on")
    private LocalDateTime createdOn;
    @Field("created_by")
    private CreatedBy createdBy;
    @Field("company_number")
    private String companyNumber;
    @Field("reason")
    private String reason;
    @Field("status")
    private ObjectionStatus status;

    public String getId() {
        return id;
    }

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }

    public CreatedBy getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(CreatedBy createdBy) {
        this.createdBy = createdBy;
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

    public ObjectionStatus getStatus() {
        return status;
    }

    public void setStatus(ObjectionStatus status) {
        this.status = status;
    }
}
