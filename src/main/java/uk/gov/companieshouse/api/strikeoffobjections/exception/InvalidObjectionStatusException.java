package uk.gov.companieshouse.api.strikeoffobjections.exception;

/** Thrown when the Objection status tries to change to an invalid state */
public class InvalidObjectionStatusException extends Exception {

    public InvalidObjectionStatusException(String message) {
        super(message);
    }
}
