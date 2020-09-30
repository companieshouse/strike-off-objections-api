package uk.gov.companieshouse.api.strikeoffobjections.model.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class CreatedByResponseDTO {

    @JsonProperty("id")
    private String id;

    @JsonProperty("email")
    private String email;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("share_identity")
    private boolean shareIdentity;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
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
