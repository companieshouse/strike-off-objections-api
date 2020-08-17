package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

public enum ObjectionStatus {
    ERROR_CHIPS(true, false),
    ERROR_EXT_EMAIL(true, false),
    ERROR_INT_EMAIL(true, false),
    OPEN(false, false),
    PROCESSED(false, false),
    RETRY_CHIPS_ONLY(false, true),
    RETRY_EXT_EMAIL_ONLY(false, true),
    RETRY_INT_EMAIL_ONLY(false, true),
    SUBMITTED(false, false);

    private boolean isErrorStatus;
    private boolean isRetryStatus;

    ObjectionStatus(boolean isErrorStatus, boolean isRetryStatus) {
        this.isErrorStatus = isErrorStatus;
        this.isRetryStatus = isRetryStatus;
    }

    public boolean isErrorStatus() {
        return isErrorStatus;
    }

    public boolean isRetryStatus() {
        return isRetryStatus;
    }

    public boolean isProcessableStatus() {
        return this.isErrorStatus || this.isRetryStatus || this == SUBMITTED;
    }
}
