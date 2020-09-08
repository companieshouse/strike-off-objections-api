package uk.gov.companieshouse.api.strikeoffobjections.model.create;

import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;

public class ObjectionUserDetails {

    private String fullName;
    private boolean shareIdentity;
    private String userEmailAddress;
    private CreatedBy createdby;

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

    public String getUserEmailAddress() {
        return userEmailAddress;
    }

    public void setUserEmailAddress(String userEmailAddress) {
        this.userEmailAddress = userEmailAddress;
    }

    public CreatedBy getCreatedby() {
        return createdby;
    }

    public void setCreatedby(CreatedBy createdby) {
        this.createdby = createdby;
    }
}
