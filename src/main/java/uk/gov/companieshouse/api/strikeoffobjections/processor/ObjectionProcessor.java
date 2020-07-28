package uk.gov.companieshouse.api.strikeoffobjections.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.LogConstants;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;

import java.util.HashMap;
import java.util.Map;

/**
 * Processes an Objection
 *
 * Will only process Objection if status = SUBMITTED
 * Calls Chips to place stop against company
 * Sends email
 */
@Component
public class ObjectionProcessor {

    private static final String INVALID_START_STATUS_MSG =
            "Objection %s has status %s. Cannot process unless status = SUBMITTED";
    private static final String LOG_OBJECTION_ID_KEY = LogConstants.OBJECTION_ID.getValue();

    private IObjectionService objectionService;
    private ApiLogger apiLogger;

    @Autowired
    public ObjectionProcessor(IObjectionService objectionService,
                              ApiLogger apiLogger) {
        this.objectionService = objectionService;
        this.apiLogger = apiLogger;
    }

    /**
     * Process the specified Objection
     * Only processes if status is SUBMITTED
     *
     * @param httpRequestId http request id used for logging
     * @param objectionId   id of the objection to process
     * @throws ObjectionNotFoundException      if objectionId not found in database
     * @throws InvalidObjectionStatusException if the Objection is not currently in status SUBMITTED when this is called
     */
    public void process(String httpRequestId, String objectionId)
            throws ObjectionNotFoundException, InvalidObjectionStatusException {

        Objection objection = getObjectionForProcessing(httpRequestId, objectionId);

        // TODO update status to processing

        // TODO OBJ-139/OBJ-20 do chips sending

        // TODO update status to chips sent

        // TODO OBJ-113 do email sending

        // TODO update status to processed

    }

    private Objection getObjectionForProcessing(String httpRequestId, String objectionId)
            throws ObjectionNotFoundException, InvalidObjectionStatusException {

        // get objection
        Objection objection = objectionService.getObjection(httpRequestId, objectionId);

        // if status not = submitted, throw exception
        if (objection != null && ObjectionStatus.SUBMITTED != objection.getStatus()) {
            InvalidObjectionStatusException statusException = new InvalidObjectionStatusException(
                    String.format(INVALID_START_STATUS_MSG, objectionId, objection.getStatus()));

            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LOG_OBJECTION_ID_KEY, objectionId);
            apiLogger.errorContext(httpRequestId, statusException.getMessage(), statusException, logMap);

            throw statusException;
        }

        return objection;
    }
}
