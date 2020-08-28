package uk.gov.companieshouse.api.strikeoffobjections.model.chips;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ChipsRequest {

    @JsonProperty("objection_id")
    private final String objectionId;

    @JsonProperty("company_number")
    private final String companyNumber;

    public ChipsRequest(String objectionId, String companyNumber) {
        this.objectionId = objectionId;
        this.companyNumber = companyNumber;
    }

    public String getObjectionId() {
        return objectionId;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    @Override
    public String toString() {
        return "ChipsRequest{" +
            "objectionId='" + objectionId + '\'' +
            ",companyNumber='" + companyNumber + '\'' +
            "}";
    }
}
