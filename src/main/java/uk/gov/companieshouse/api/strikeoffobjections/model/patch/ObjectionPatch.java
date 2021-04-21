package uk.gov.companieshouse.api.strikeoffobjections.model.patch;

import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ObjectionPatch {

    @JsonProperty("objector")
    private String objector;
    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("share_identity")
    private Boolean shareIdentity;
    private String reason;
    private ObjectionStatus status;

    public String getObjector() {
        return objector;
    }

    public void setObjector(final String objector) {
        this.objector = objector;
    }

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public Boolean isShareIdentity() {
        return shareIdentity;
    }

    public void setShareIdentity(Boolean shareIdentity) {
        this.shareIdentity = shareIdentity;
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
