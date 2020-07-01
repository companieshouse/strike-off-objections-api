package uk.gov.companieshouse.api.strikeoffobjections.model.request;

import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

public class ObjectionRequest {
    private String reason;
    private ObjectionStatus status;

    public ObjectionRequest() {
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
