package uk.gov.companieshouse.api.strikeoffobjections.model.patcher;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;

import java.time.LocalDateTime;
import java.util.function.Supplier;

@Component
public class ObjectionPatcher {

    @Autowired
    private Supplier<LocalDateTime> dateTimeSupplier;

    public ObjectionPatcher(Supplier<LocalDateTime> dateTimeSupplier) {
        this.dateTimeSupplier = dateTimeSupplier;
    }

    public Objection patchObjection(ObjectionPatch objectionPatch, String requestId, Objection existingObjection) {
        existingObjection.setHttpRequestId(requestId);
        if (objectionPatch.getReason() != null) {
            existingObjection.setReason(objectionPatch.getReason());
        }

        if (objectionPatch.getStatus() != null) {
            existingObjection.setStatus(objectionPatch.getStatus());
            existingObjection.setStatusChangedOn(dateTimeSupplier.get());
        }

        return existingObjection;
    }
}
