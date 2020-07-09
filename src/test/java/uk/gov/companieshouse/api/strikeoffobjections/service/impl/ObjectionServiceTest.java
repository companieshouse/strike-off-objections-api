package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.model.patcher.ObjectionPatcher;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;
import uk.gov.companieshouse.api.strikeoffobjections.repository.ObjectionRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ObjectionServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String REQUEST_ID = "87654321";
    private static final String AUTH_ID = "22334455";
    private static final String E_MAIL = "demo@ch.gov.uk";
    private static final String AUTH_USER = E_MAIL + "; forename=demoForename; surname=demoSurname";
    private static final String OBJECTION_ID = "87651234";
    private static final String REASON = "REASON";
    private static final LocalDateTime MOCKED_TIME_STAMP = LocalDateTime.of(2020, 2,2, 0, 0);

    @Mock
    ApiLogger apiLogger;

    @Mock
    ObjectionRepository objectionRepository;

    @Mock
    Supplier<LocalDateTime> localDateTimeSupplier;

    @Mock
    ObjectionPatcher objectionPatcher;

    @Mock
    private ERICHeaderParser ericHeaderParser;

    @InjectMocks
    ObjectionService objectionService;

    @Test
    void createObjectionTest() throws Exception {
        Objection returnedEntity = new Objection.Builder()
                .withCompanyNumber(COMPANY_NUMBER)
                .build();
        returnedEntity.setId(OBJECTION_ID);
        when(objectionRepository.save(any())).thenReturn(returnedEntity);
        when(localDateTimeSupplier.get()).thenReturn(MOCKED_TIME_STAMP);
        when(ericHeaderParser.getEmailAddress(AUTH_USER)).thenReturn(E_MAIL);

        ArgumentCaptor<Objection> acObjection = ArgumentCaptor.forClass(Objection.class);
        String returnedId = objectionService.createObjection(REQUEST_ID, COMPANY_NUMBER, AUTH_ID, AUTH_USER);

        verify(objectionRepository).save(acObjection.capture());
        assertEquals(OBJECTION_ID, returnedId);
        assertEquals(MOCKED_TIME_STAMP, acObjection.getValue().getCreatedOn());
        assertEquals(COMPANY_NUMBER, acObjection.getValue().getCompanyNumber());
        assertEquals(AUTH_ID, acObjection.getValue().getCreatedBy().getId());
        assertEquals(E_MAIL, acObjection.getValue().getCreatedBy().getEmail());
    }

    @Test
    void patchObjectionExistsTest() throws Exception {
        Objection existingObjection = new Objection();
        existingObjection.setId(OBJECTION_ID);
        Objection objection = new Objection();
        objection.setId(OBJECTION_ID);
        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(ObjectionStatus.OPEN);
        when(objectionRepository.findById(any())).thenReturn(Optional.of(existingObjection));
        when(objectionPatcher.patchObjection(any(), any(), any())).thenReturn(objection);

        objectionService.patchObjection(REQUEST_ID, COMPANY_NUMBER, OBJECTION_ID, objectionPatch);

        verify(objectionRepository, times(1)).save(objection);
    }

    @Test
    void patchObjectionDoesNotExistTest() throws Exception {
        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(ObjectionStatus.OPEN);
        when(objectionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ObjectionNotFoundException.class, () -> objectionService.patchObjection(REQUEST_ID, COMPANY_NUMBER, OBJECTION_ID, objectionPatch));

        verify(objectionRepository, times(0)).save(any());
    }
}
