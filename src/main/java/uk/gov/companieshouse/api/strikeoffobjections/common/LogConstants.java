package uk.gov.companieshouse.api.strikeoffobjections.common;

public enum LogConstants {

    COMPANY_NUMBER("company_number"),
    REQUEST_ID("request_id"),
    REASON("reason");

    private String value;

    LogConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
