package uk.gov.companieshouse.api.strikeoffobjections.model.patcher;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.request.ObjectionRequest;

@Component
public class ObjectionPatcher {

    public Objection patchObjection(ObjectionRequest objectionRequest, String requestId, Objection existingObjection) {
        existingObjection.setHttpRequestId(requestId);
        if (objectionRequest.getReason() != null) {
            existingObjection.setReason(objectionRequest.getReason());
        }

        if (objectionRequest.getStatus() != null) {
            existingObjection.setStatus(objectionRequest.getStatus());
        }

        return existingObjection;
    }
}
