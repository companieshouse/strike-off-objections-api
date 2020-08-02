package uk.gov.companieshouse.api.strikeoffobjections.model.patcher;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;

@Component
public class ObjectionPatcher {

    public Objection patchObjection(ObjectionPatch objectionPatch, String requestId, Objection existingObjection) {
        existingObjection.setHttpRequestId(requestId);
        if (objectionPatch.getReason() != null) {
            existingObjection.setReason(objectionPatch.getReason());
        }

        ObjectionStatus existingStatus = existingObjection.getStatus();
        if (existingStatus == null) {
            //TODO log and throw error
        }

        if (isStatusChangeAllowed(existingStatus, objectionPatch)) {
            existingObjection.setStatus(objectionPatch.getStatus());
        }

        return existingObjection;
    }

    // if incoming status == SUBMITTED and existing status is an error status then don't change it
    private boolean isStatusChangeAllowed(ObjectionStatus existingStatus, ObjectionPatch objectionPatch) {

        ObjectionStatus incomingStatus = objectionPatch.getStatus();
        if (incomingStatus == null) {
            return false;
        }

        return existingStatus == ObjectionStatus.OPEN && incomingStatus == ObjectionStatus.SUBMITTED;
    }
}
