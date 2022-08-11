package uk.gov.companieshouse.api.strikeoffobjections.exception;

public class UnsafeUrlException extends RuntimeException {
    public UnsafeUrlException(ExceptionType exceptionType, String... content) {
        super(String.format(exceptionType.getMessage(), content));
    }

    public enum ExceptionType {

        UNSAFE_COMPANY_NUMBER("Unsafe Company Number [%s] is passed"),
        UNSAFE_URL("Url [%s] is corrupted");

        private final String message;

        ExceptionType(String message) {
            this.message = message;
        }
        
        public String getMessage() {
            return message;
        }
    }
}
