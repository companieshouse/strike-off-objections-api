package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

import org.springframework.data.mongodb.core.mapping.Field;

public class CreatedBy {

    @Field("id")
    private String id;

    @Field("email")
    private String email;

    public CreatedBy(String id, String email) {
        this.id = id;
        this.email = email;
    }
    
    public String getId() {
        return id;
    }

    public String getEmail() {
        return email;
    }
}
