package uk.gov.companieshouse.api.strikeoffobjections.model.patcher;

import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Unit
class ObjectionPatcherTest {

    private static final String REASON = "REASON";
    private static final String OBJECTION_ID = "OBJECTION_ID";
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    private static final String REQUEST_ID = "REQUEST_ID";
    private static final LocalDateTime CREATED_ON = LocalDateTime.of(2020, 1, 1, 1, 1);

    private ObjectionPatcher objectionPatcher = new ObjectionPatcher();

    @Test
    void patchObjectionTest() {
        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(ObjectionStatus.SUBMITTED);

        Objection existingObjection = new Objection();
        existingObjection.setCreatedOn(CREATED_ON);
        existingObjection.setId(OBJECTION_ID);
        existingObjection.setCompanyNumber(COMPANY_NUMBER);
        existingObjection.setStatus(ObjectionStatus.OPEN);

        Objection objection = objectionPatcher.patchObjection(objectionPatch, REQUEST_ID, existingObjection);

        assertEquals(REASON, objection.getReason());
        assertEquals(OBJECTION_ID, objection.getId());
        assertEquals(COMPANY_NUMBER, objection.getCompanyNumber());
        assertEquals(ObjectionStatus.SUBMITTED, objection.getStatus());
        assertEquals(REQUEST_ID, objection.getHttpRequestId());
        assertEquals(CREATED_ON, objection.getCreatedOn());
    }

    // TODO more patch status tests eg, no change ones, error ones
}
