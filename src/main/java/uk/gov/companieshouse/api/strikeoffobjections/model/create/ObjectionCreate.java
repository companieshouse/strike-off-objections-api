package uk.gov.companieshouse.api.strikeoffobjections.model.create;
import com.fasterxml.jackson.annotation.JsonProperty;

public class ObjectionCreate {

    @JsonProperty("full_name")
    private String fullName;
    @JsonProperty("share_identity")
    private boolean shareIdentity;

    public String getFullName() {
        return fullName;
    }

    public void setFullName(String fullName) {
        this.fullName = fullName;
    }

    public boolean canShareIdentity() {
        return shareIdentity;
    }

    public void setShareIdentity(boolean shareIdentity) {
        this.shareIdentity = shareIdentity;
    }
}
