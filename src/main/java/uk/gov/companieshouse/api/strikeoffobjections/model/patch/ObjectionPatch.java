package uk.gov.companieshouse.api.strikeoffobjections.model.patch;

import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

public class ObjectionPatch {
    private String reason;
    private ObjectionStatus status;

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
