package uk.gov.companieshouse.api.strikeoffobjections.model.response;

public class ObjectionResponse {
    private final String id;

    public ObjectionResponse(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
