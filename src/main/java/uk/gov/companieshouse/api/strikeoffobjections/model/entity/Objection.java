package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Document(collection = "strike_off_objections")
public class Objection {


    public static class Builder {
        private LocalDateTime createdOn;
        private CreatedBy createdBy;
        private String companyNumber;
        private String reason;
        private ObjectionStatus status;
        private String httpRequestId;

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

        public Builder withHttpRequestId(String httpRequestId) {
            this.httpRequestId = httpRequestId;
            return this;
        }

        public Objection build() {
            Objection objection = new Objection();
            objection.setCreatedOn(this.createdOn);
            objection.setCreatedBy(this.createdBy);
            objection.setCompanyNumber(this.companyNumber);
            objection.setReason(this.reason);
            objection.setStatus(this.status);
            objection.setHttpRequestId(this.httpRequestId);
            return objection;
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
    @Field("attachments")
    private List<Attachment> attachments = new ArrayList<>();
    @Field("status")
    private ObjectionStatus status;
    @Field("http_request_id")
    @JsonIgnore
    private String httpRequestId;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
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

    public void addAttachment(Attachment attachment) {
        if (attachments != null) {
            attachments.add(attachment);
        }
    }

    public List<Attachment> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<Attachment> attachments) {
        this. attachments =  attachments;
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
