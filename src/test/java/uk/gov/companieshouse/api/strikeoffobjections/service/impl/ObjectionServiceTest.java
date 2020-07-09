package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClient;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.model.patcher.ObjectionPatcher;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;
import uk.gov.companieshouse.api.strikeoffobjections.repository.ObjectionRepository;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.service.ServiceException;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ObjectionServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String REQUEST_ID = "87654321";
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
    private FileTransferApiClient fileTransferApiClient;

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

        ArgumentCaptor<Objection> acObjection = ArgumentCaptor.forClass(Objection.class);
        String returnedId = objectionService.createObjection(REQUEST_ID, COMPANY_NUMBER);

        verify(objectionRepository).save(acObjection.capture());
        assertEquals(OBJECTION_ID, returnedId);
        assertEquals(MOCKED_TIME_STAMP, acObjection.getValue().getCreatedOn());
        assertEquals(COMPANY_NUMBER, acObjection.getValue().getCompanyNumber());
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



    @Test
    public void willThrowServiceExceptionIfUploadErrors() throws Exception {
        when(fileTransferApiClient.upload(anyString(), any(MultipartFile.class))).thenReturn(Utils.getUnsuccessfulUploadResponse());

        try {
            objectionService.addAttachment(OBJECTION_ID, Utils.mockMultipartFile());
            fail();
        } catch(ServiceException e) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
        }
    }

    @Test
    public void willPropagateServerRuntimeExceptions() throws Exception {
        when(fileTransferApiClient.upload(anyString(), any(MultipartFile.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));
        try {
            objectionService.addAttachment(OBJECTION_ID, Utils.mockMultipartFile());
            fail();
        } catch(HttpServerErrorException e) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
        }
    }

    @Test
    public void willPropagateClientRuntimeExceptions() throws Exception {
        when(fileTransferApiClient.upload(anyString(), any(MultipartFile.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));
        try {
            objectionService.addAttachment(OBJECTION_ID, Utils.mockMultipartFile());
            fail();
        } catch(HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST.toString(), e.getMessage());
        }
    }
}
