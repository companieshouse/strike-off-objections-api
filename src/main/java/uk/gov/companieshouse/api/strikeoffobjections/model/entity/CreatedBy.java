package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

import org.springframework.data.mongodb.core.mapping.Field;

public class CreatedBy {

    @Field("id")
    private String id;

    @Field("email")
    private String email;

    @Field("objector")
    private String objector;

    @Field("full_name")
    private String fullName;

    @Field("share_identity")
    private boolean shareIdentity;

    public CreatedBy(String id, String email, String objector, String fullName, boolean shareIdentity) {
        this.id = id;
        this.email = email;
        this.objector = objector;
        this.fullName = fullName;
        this.shareIdentity = shareIdentity;
    }
    
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

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

    public boolean isShareIdentity() {
        return shareIdentity;
    }

    public void setShareIdentity(boolean shareIdentity) {
        this.shareIdentity = shareIdentity;
    }
}
