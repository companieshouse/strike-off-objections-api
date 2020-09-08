package uk.gov.companieshouse.api.strikeoffobjections.model.patch;

import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

import java.time.LocalDateTime;

public class ObjectionPatch {
    private String reason;
    private ObjectionStatus status;
    private LocalDateTime createdOn;

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

    public LocalDateTime getCreatedOn() {
        return createdOn;
    }

    public void setCreatedOn(LocalDateTime createdOn) {
        this.createdOn = createdOn;
    }
}
