package uk.gov.companieshouse.api.strikeoffobjections.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.LogConstants;
import uk.gov.companieshouse.api.strikeoffobjections.exception.InvalidObjectionStatusException;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

import java.util.HashMap;
import java.util.Map;

/**
 * Processes an Objection
 * <p>
 * Will only process Objection if status = SUBMITTED
 * Calls Chips to place stop against company
 * Sends email
 */
@Component
public class ObjectionProcessor {

    private static final String INVALID_START_STATUS_MSG =
            "Objection %s has status %s. Cannot process unless status = SUBMITTED";
    private static final String LOG_OBJECTION_ID_KEY = LogConstants.OBJECTION_ID.getValue();

    private ApiLogger apiLogger;

    @Autowired
    public ObjectionProcessor(ApiLogger apiLogger) {
        this.apiLogger = apiLogger;
    }

    /**
     * Process the specified Objection
     * Only processes if status is SUBMITTED
     *
     * @param objection     the objection to process
     * @param httpRequestId http request id used for logging
     * @throws InvalidObjectionStatusException if the Objection is not currently in status SUBMITTED when this is called
     */
    public void process(Objection objection, String httpRequestId)
            throws InvalidObjectionStatusException {

        if (objection == null) {
            throw new IllegalArgumentException(
                    "Objection arg missing from ObjectionProcessor.process(Objection, String)");
        }
        if (httpRequestId == null) {
            throw new IllegalArgumentException(
                    "httpRequestId arg missing from ObjectionProcessor.process(Objection, String)");
        }

        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LOG_OBJECTION_ID_KEY, objection.getId());
        apiLogger.debugContext(httpRequestId, "Starting objection processing", logMap);

        validateObjectionStatus(objection, httpRequestId);

        // TODO update status to processing

        // TODO OBJ-139/OBJ-20 do chips sending

        // TODO update status to chips sent

        // TODO OBJ-157 do email sending

        // TODO update status to processed

    }

    private void validateObjectionStatus(Objection objection, String httpRequestId)
            throws InvalidObjectionStatusException {

        // if status not SUBMITTED, throw exception
        if (objection != null && ObjectionStatus.SUBMITTED != objection.getStatus()) {
            InvalidObjectionStatusException statusException = new InvalidObjectionStatusException(
                    String.format(INVALID_START_STATUS_MSG, objection.getId(), objection.getStatus()));

            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LOG_OBJECTION_ID_KEY, objection.getId());
            apiLogger.errorContext(httpRequestId, statusException.getMessage(), statusException, logMap);

            throw statusException;
        }
    }
}
