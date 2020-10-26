package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

public enum ObjectionStatus {

    INELIGIBLE_COMPANY_STRUCK_OFF,
    INELIGIBLE_NO_DISSOLUTION_ACTION,
    OPEN,
    PROCESSED,
    ERROR_DATA_INCOMPLETE,
    ERROR_CHIPS,
    ERROR_INTERNAL_EMAIL,
    ERROR_EXTERNAL_EMAIL,
    SUBMITTED;

    public boolean isIneligible() {
        return this == INELIGIBLE_NO_DISSOLUTION_ACTION ||
                this == INELIGIBLE_COMPANY_STRUCK_OFF;
    }
}
