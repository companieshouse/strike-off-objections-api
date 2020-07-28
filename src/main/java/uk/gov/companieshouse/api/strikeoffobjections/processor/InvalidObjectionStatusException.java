package uk.gov.companieshouse.api.strikeoffobjections.processor;

public class InvalidObjectionStatusException extends Exception {

     public InvalidObjectionStatusException(String message) {
         super(message);
     }
}
