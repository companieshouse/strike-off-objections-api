package uk.gov.companieshouse.api.strikeoffobjections.model.request;

import org.springframework.data.mongodb.core.mapping.Field;

public class CreatedBy {

    @Field("id")
    private String id;

    @Field("email")
    private String email;

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
}
