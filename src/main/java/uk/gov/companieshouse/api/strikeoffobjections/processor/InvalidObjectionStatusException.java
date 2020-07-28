package uk.gov.companieshouse.api.strikeoffobjections.processor;

/**
 * Thrown when the Objection status is in an invalid state
 */
public class InvalidObjectionStatusException extends Exception {

     public InvalidObjectionStatusException(String message) {
         super(message);
     }
}
