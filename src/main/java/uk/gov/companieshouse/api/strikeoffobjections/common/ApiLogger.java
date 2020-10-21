package uk.gov.companieshouse.api.strikeoffobjections.common;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.Application;
import uk.gov.companieshouse.logging.Logger;
import uk.gov.companieshouse.logging.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

/**
 * Acts as a wrapper for the structured logger to help with unit testing and also ensures that the
 * map data structure passed to the Companies House logger is not changed if used by subsequent
 * logging calls.
 */
@Component
public class ApiLogger {

    private static final Logger LOGGER = LoggerFactory.getLogger(Application.APP_NAMESPACE);

    public void debugContext(String context, String message) {
        LOGGER.debugContext(context, message, null);
    }

    public void debugContext(String context, String message, Map<String, Object> dataMap) {
        LOGGER.debugContext(context, message, cloneMapData(dataMap));
    }

    public void info(String message) {
        LOGGER.info(message, null);
    }

    public void infoContext(String context, String message) {
        LOGGER.infoContext(context, message, null);
    }

    public void infoContext(String context, String message, Map<String, Object> dataMap) {
        LOGGER.infoContext(context, message, cloneMapData(dataMap));
    }

    public void errorContext(String context, Exception e) {
        LOGGER.errorContext(context, e, null);
    }

    public void errorContext(String context, String message, Exception e) {
        LOGGER.errorContext(context, message, e, null);
    }

    public void errorContext(String context, String message, Exception e, Map<String, Object> dataMap) {
        LOGGER.errorContext(context, message, e, cloneMapData(dataMap));
    }

    /**
     * The Companies House logging implementation modifies the data map content which means that
     * if the same data map is used for subsequent calls any new message that might be passed in
     * is not displayed in certain log format outputs. Creating a clone of the data map gets around
     * this issue.
     *  
     * @param dataMap The map data to log
     * @return A cloned copy of the map data
     */
    private Map<String, Object> cloneMapData(Map<String, Object> dataMap) {
        Map<String, Object> clonedMapData = new HashMap<>();
        clonedMapData.putAll(dataMap);

        return clonedMapData;
    }
}
