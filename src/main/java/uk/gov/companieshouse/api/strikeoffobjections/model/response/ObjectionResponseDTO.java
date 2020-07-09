package uk.gov.companieshouse.api.strikeoffobjections.model.response;

public class ObjectionResponseDTO {
    private final String id;

    public ObjectionResponseDTO(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }
}
