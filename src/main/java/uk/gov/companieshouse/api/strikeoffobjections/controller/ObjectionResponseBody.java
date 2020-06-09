package uk.gov.companieshouse.api.strikeoffobjections.controller;

import com.fasterxml.jackson.annotation.JsonProperty;

public class ObjectionResponseBody {

    @JsonProperty("request_id")
    private String requestId;

    public String getRequestId() {
        return requestId;
    }

    public void setRequestId(String requestId) {
        this.requestId = requestId;
    }

    @Override
    public String toString() {
        return "ObjectionResponseBody["
            + "requestId = " + requestId
            + "]";
    }
}
