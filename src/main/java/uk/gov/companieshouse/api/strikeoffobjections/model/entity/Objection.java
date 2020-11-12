package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

import com.fasterxml.jackson.annotation.JsonIgnore;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;
import org.springframework.util.StringUtils;
import uk.gov.companieshouse.service.links.Links;

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
        private Long actionCode;
        private String httpRequestId;
        private LocalDateTime statusChangedOn;
        private String id;
        private Links links;

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

        public Builder withActionCode(Long actionCode) {
            this.actionCode = actionCode;
            return this;
        }

        public Builder withHttpRequestId(String httpRequestId) {
            this.httpRequestId = httpRequestId;
            return this;
        }

        public Builder withStatusChangedOn(LocalDateTime statusChangedOn) {
            this.statusChangedOn = statusChangedOn;
            return this;
        }

        public Builder withId(String id) {
            this.id = id;
            return this;
        }

        public Builder withLinks(Links links) {
            this.links = links;
            return this;
        }

        public Objection build() {
            Objection objection = new Objection();
            objection.setCreatedOn(this.createdOn);
            objection.setCreatedBy(this.createdBy);
            objection.setCompanyNumber(this.companyNumber);
            objection.setReason(this.reason);
            objection.setStatus(this.status);
            objection.setActionCode(actionCode);
            objection.setHttpRequestId(this.httpRequestId);
            objection.setStatusChangedOn(this.statusChangedOn);
            objection.setId(this.id);
            objection.setLinks(this.links);
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
    @Field("action_code")
    private Long actionCode;
    @Field("http_request_id")
    @JsonIgnore
    private String httpRequestId;
    @Field("status_changed_on")
    private LocalDateTime statusChangedOn;
    @Field("links")
    private Links links;

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
        this. attachments = attachments;
    }

    public ObjectionStatus getStatus() {
        return status;
    }

    public void setStatus(ObjectionStatus status) {
        this.status = status;
    }

    public Long getActionCode() {
        return actionCode;
    }

    public void setActionCode(Long actionCode) {
        this.actionCode = actionCode;
    }

    public String getHttpRequestId() {
        return httpRequestId;
    }

    public void setHttpRequestId(String httpRequestId) {
        this.httpRequestId = httpRequestId;
    }

    public LocalDateTime getStatusChangedOn() {
        return statusChangedOn;
    }

    public void setStatusChangedOn(LocalDateTime statusChangedOn) {
        this.statusChangedOn = statusChangedOn;
    }

    public boolean isDataEnteredByUserIncomplete() {
        return StringUtils.isEmpty(createdBy.getFullName())
                || attachments.isEmpty();
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }
}
