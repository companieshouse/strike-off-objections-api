package uk.gov.companieshouse.api.strikeoffobjections.model.patcher;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;

@Component
public class ObjectionPatcher {

    public Objection patchObjection(ObjectionPatch objectionPatch, String requestId, Objection existingObjection) {
        existingObjection.setHttpRequestId(requestId);
        if (objectionPatch.getReason() != null) {
            existingObjection.setReason(objectionPatch.getReason());
        }

        if (objectionPatch.getStatus() != null) {
            existingObjection.setStatus(objectionPatch.getStatus());
        }

        return existingObjection;
    }
}
