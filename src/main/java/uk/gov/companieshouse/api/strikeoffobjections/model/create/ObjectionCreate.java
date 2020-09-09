package uk.gov.companieshouse.api.strikeoffobjections.model.create;

public class ObjectionCreate {

    private String fullName;
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
