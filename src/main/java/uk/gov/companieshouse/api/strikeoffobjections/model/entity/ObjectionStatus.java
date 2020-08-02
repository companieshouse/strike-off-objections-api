package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

public enum ObjectionStatus {
    ERROR_CHIPS(true),
    ERROR_EXT_EMAIL(true),
    ERROR_INT_EMAIL(true),
    OPEN(false),
    PROCESSED(false),
    RETRY_CHIPS_ONLY(true),
    RETRY_EXT_EMAIL_ONLY(true),
    RETRY_INT_EMAIL_ONLY(true),
    SUBMITTED(false);

    private boolean isErrorStatus;

    ObjectionStatus(boolean isErrorStatus) {
        this.isErrorStatus = isErrorStatus;
    }

    public boolean isErrorStatus() {
        return isErrorStatus;
    }

    public boolean isProcessableStatus() {
        return this.isErrorStatus || this == SUBMITTED;
    }


}
