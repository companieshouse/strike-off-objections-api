package uk.gov.companieshouse.api.strikeoffobjections.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;

/**
 * Processes an Objection
 * Will only process Objection is status = SUBMITTED
 * Calls Chips to place stop against company
 * Sends email
 */
@Component
public class ObjectionProcessor {

    private IObjectionService objectionService;

    @Autowired
    public ObjectionProcessor(IObjectionService objectionService) {
        this.objectionService = objectionService;
    }

    /**
     * Process the specified Objection
     * Only processes if status is SUBMITTED
     *
     * @param httpRequestId http request id used for logging
     * @param objectionId   id of the objection to process
     * @throws ObjectionNotFoundException if objectionId not found in database
     */
    public void process(String httpRequestId, String objectionId) throws ObjectionNotFoundException {

        // TODO if status != submitted, return (or throw ?)

        // get objection
        Objection objection = objectionService.getObjection(httpRequestId, objectionId);

        // TODO update status to processing

        // TODO OBJ-139/OBJ-20 do chips sending

        // TODO update status to chips sent

        // TODO OBJ-113 do email sending

        // TODO update status to processed

    }
}
