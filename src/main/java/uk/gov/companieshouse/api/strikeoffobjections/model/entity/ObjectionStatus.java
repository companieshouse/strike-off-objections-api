package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

public enum ObjectionStatus {

    INELIGIBLE_COMPANY_STRUCK_OFF,
    INELIGIBLE_NO_DISSOLUTION_ACTION,
    OPEN,
    PROCESSED,
    PROCESSING,
    CHIPS_SENT,
    INTERNAL_EMAIL_SENT,
    SUBMITTED;

    public boolean isIneligible() {
        return this == INELIGIBLE_NO_DISSOLUTION_ACTION ||
                this == INELIGIBLE_COMPANY_STRUCK_OFF;
    }
}
