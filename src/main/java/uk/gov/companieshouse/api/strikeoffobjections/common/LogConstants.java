package uk.gov.companieshouse.api.strikeoffobjections.common;

public enum LogConstants {

    ACTION_CODE("action_code"),
    ATTACHMENT_ID("attachment_id"),
    COMPANY_NUMBER("company_number"),
    OBJECTION_ID("objection_id"),
    OBJECTION_STATUS("objection_status");

    private String value;

    LogConstants(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
