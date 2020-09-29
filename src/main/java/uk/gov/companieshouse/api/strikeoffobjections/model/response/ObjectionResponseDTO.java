package uk.gov.companieshouse.api.strikeoffobjections.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

import java.util.List;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ObjectionResponseDTO {

    @JsonProperty("id")
    private String id;

    @JsonProperty("created_on")
    private String createdOn;

    @JsonProperty("created_by")
    private CreatedByResponseDTO createdBy;

    @JsonProperty("company_number")
    private String companyNumber;

    @JsonProperty("reason")
    private String reason;

    @JsonProperty("attachments")
    private List<AttachmentResponseDTO> attachments;

    @JsonProperty("status")
    private ObjectionStatus status;

    public ObjectionResponseDTO(String id) {
        this.id = id;
    }

    public ObjectionResponseDTO() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(String createdOn) {
        this.createdOn = createdOn;
    }

    public CreatedByResponseDTO getCreatedBy() {
        return createdBy;
    }

    public void setCreatedBy(CreatedByResponseDTO createdBy) {
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

    public List<AttachmentResponseDTO> getAttachments() {
        return attachments;
    }

    public void setAttachments(List<AttachmentResponseDTO> attachments) {
        this.attachments = attachments;
    }

    public ObjectionStatus getStatus() {
        return status;
    }

    public void setStatus(ObjectionStatus status) {
        this.status = status;
    }
}
