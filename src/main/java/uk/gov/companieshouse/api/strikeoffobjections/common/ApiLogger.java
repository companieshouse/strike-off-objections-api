package uk.gov.companieshouse.api.strikeoffobjections.common;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.Application;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.Map;

/**
 * Acts as a wrapper for the structured logger to help with unit testing
 */
@Component
public class ApiLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.APP_NAMESPACE);

    public void debugContext(String context, String message) {
        LOGGER.debugContext(context, message, null);
    }

    public void debugContext(String context, String message, Map<String, Object> dataMap) {
        LOGGER.debugContext(context, message, dataMap);
    }

    public void info(String message) {
        LOGGER.info(message, null);
    }

    public void infoContext(String context, String message) {
        LOGGER.infoContext(context, message, null);
    }

    public void infoContext(String context, String message, Map<String, Object> dataMap) {
        LOGGER.infoContext(context, message, dataMap);
    }

    public void errorContext(String context, Exception e) {
        LOGGER.errorContext(context, e, null);
    }

    public void errorContext(String context, String message, Exception e) {
        LOGGER.errorContext(context, message, e, null);
    }

    public void errorContext(String context, String message, Exception e, Map<String, Object> dataMap) {
        LOGGER.errorContext(context, message, e, dataMap);
    }
}
