package uk.gov.companieshouse.api.strikeoffobjections.model.patcher;

import org.junit.jupiter.api.Test;

import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class ObjectionPatcherTest {

    private static final String REASON = "REASON";
    private static final String OBJECTION_ID = "OBJECTION_ID";
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    private static final String REQUEST_ID = "REQUEST_ID";
    private static final String FULL_NAME = "Joe Bloggs";
    private static final LocalDateTime CREATED_ON = LocalDateTime.of(2020, 1, 1, 1, 1);
    private static final LocalDateTime STATUS_CHANGED_ON = LocalDateTime.of(2021, 1, 1, 1, 1);

    @Mock
    Supplier<LocalDateTime> dateTimeSupplier;

    @InjectMocks
    private ObjectionPatcher objectionPatcher;

    @Test
    void requestToObjectionCreationTest() {
        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setFullName(FULL_NAME);
        objectionPatch.setShareIdentity(Boolean.TRUE);
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(ObjectionStatus.OPEN);

        CreatedBy createdBy = new CreatedBy( "", "","Not Joe Bloggs", Boolean.FALSE);
        Objection existingObjection = new Objection();
        existingObjection.setCreatedOn(CREATED_ON);
        existingObjection.setId(OBJECTION_ID);
        existingObjection.setCreatedBy(createdBy);
        existingObjection.setCompanyNumber(COMPANY_NUMBER);

        when(dateTimeSupplier.get()).thenReturn(STATUS_CHANGED_ON);
        Objection objection = objectionPatcher.patchObjection(objectionPatch, REQUEST_ID, existingObjection);

        assertEquals(REASON, objection.getReason());
        assertEquals(OBJECTION_ID, objection.getId());
        assertEquals(COMPANY_NUMBER, objection.getCompanyNumber());
        assertEquals(FULL_NAME, objection.getCreatedBy().getFullName());
        assertEquals(Boolean.TRUE, objection.getCreatedBy().isShareIdentity());
        assertEquals(ObjectionStatus.OPEN, objection.getStatus());
        assertEquals(REQUEST_ID, objection.getHttpRequestId());
        assertEquals(CREATED_ON, objection.getCreatedOn());
        assertEquals(STATUS_CHANGED_ON, objection.getStatusChangedOn());
    }

    @Test
    void testShareIdNotChangedWhenNullInPatch() {
        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setFullName(FULL_NAME);
        objectionPatch.setShareIdentity(null);
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(ObjectionStatus.OPEN);

        CreatedBy createdBy = new CreatedBy( "", "","Not Joe Bloggs", Boolean.TRUE);
        Objection existingObjection = new Objection();
        existingObjection.setCreatedOn(CREATED_ON);
        existingObjection.setId(OBJECTION_ID);
        existingObjection.setCreatedBy(createdBy);
        existingObjection.setCompanyNumber(COMPANY_NUMBER);

        when(dateTimeSupplier.get()).thenReturn(STATUS_CHANGED_ON);
        Objection objection = objectionPatcher.patchObjection(objectionPatch, REQUEST_ID, existingObjection);

        assertEquals(REASON, objection.getReason());
        assertEquals(OBJECTION_ID, objection.getId());
        assertEquals(COMPANY_NUMBER, objection.getCompanyNumber());
        assertEquals(FULL_NAME, objection.getCreatedBy().getFullName());
        assertEquals(Boolean.TRUE, objection.getCreatedBy().isShareIdentity());
        assertEquals(ObjectionStatus.OPEN, objection.getStatus());
        assertEquals(REQUEST_ID, objection.getHttpRequestId());
        assertEquals(CREATED_ON, objection.getCreatedOn());
        assertEquals(STATUS_CHANGED_ON, objection.getStatusChangedOn());
    }
}
