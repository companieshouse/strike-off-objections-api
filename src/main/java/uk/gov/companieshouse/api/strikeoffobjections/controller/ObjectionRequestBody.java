package uk.gov.companieshouse.api.strikeoffobjections.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class ObjectionRequestBody {

    @JsonProperty("reason")
    @NotNull(message = "Missing reason")
    private String reason;

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }

    @Override
    public String toString() {
        return "ObjectionRequestBody["
            + "reason = " + reason
            + "]";
    }
}
