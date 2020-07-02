package uk.gov.companieshouse.api.strikeoffobjections.common;

public enum LogConstants {

    COMPANY_NUMBER("company_number"),
    OBJECTION_ID("objection_id"),
    REASON("reason");

    private String value;

    LogConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
