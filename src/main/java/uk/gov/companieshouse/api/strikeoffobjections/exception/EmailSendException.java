package uk.gov.companieshouse.api.strikeoffobjections.exception;

public class EmailSendException extends RuntimeException {
    public EmailSendException(String failedToSendEmail, Exception e) {
        super(failedToSendEmail, e);
    }
}
