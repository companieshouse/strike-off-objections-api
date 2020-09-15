package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

import org.springframework.data.mongodb.core.mapping.Field;

public class CreatedBy {

    @Field("id")
    private String id;

    @Field("email")
    private String email;

    @Field("full_name")
    private String fullName;

    @Field("share_identity")
    private boolean shareIdentity;

    public CreatedBy(String id, String email, String fullName, boolean shareIdentity) {
        this.id = id;
        this.email = email;
        this.fullName = fullName;
        this.shareIdentity = shareIdentity;
    }
    
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }

    public String getFullName() {
        return fullName;
    }

    public boolean isShareIdentity() {
        return shareIdentity;
    }
}
