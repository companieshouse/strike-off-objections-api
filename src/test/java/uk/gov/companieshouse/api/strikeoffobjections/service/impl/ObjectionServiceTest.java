package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import uk.gov.companieshouse.api.strikeoffobjections.client.OracleQueryClient;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.AttachmentNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.exception.InvalidObjectionStatusException;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClient;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.api.strikeoffobjections.file.ObjectionsLinkKeys;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.eligibility.EligibilityStatus;
import uk.gov.companieshouse.api.strikeoffobjections.model.eligibility.ObjectionEligibility;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.model.patcher.ObjectionPatcher;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;
import uk.gov.companieshouse.api.strikeoffobjections.processor.ObjectionProcessor;
import uk.gov.companieshouse.api.strikeoffobjections.repository.ObjectionRepository;
import uk.gov.companieshouse.api.strikeoffobjections.service.IReferenceNumberGeneratorService;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.api.strikeoffobjections.validation.ActionCodeValidator;
import uk.gov.companieshouse.api.strikeoffobjections.validation.Gaz2RequestedValidator;
import uk.gov.companieshouse.api.strikeoffobjections.validation.ValidationException;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.only;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;
import static uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus.INELIGIBLE_COMPANY_STRUCK_OFF;
import static uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus.OPEN;

@Unit
@ExtendWith(MockitoExtension.class)
class ObjectionServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String REQUEST_ID = "87654321";
    private static final String AUTH_ID = "22334455";
    private static final String E_MAIL = "demo@ch.gov.uk";
    private static final String AUTH_USER = E_MAIL + "; forename=demoForename; surname=demoSurname";
    private static final String OBJECTION_ID = "87651234";
    private static final String ATTACHMENT_ID = "12348765";
    private static final String REASON = "REASON";
    private static final String ACCESS_URL = "/dummyUrl";
    private static final Long ACTION_CODE_OK = 3000L;
    private static final Long ACTION_CODE_INELIGIBLE = 200L;
    private static final LocalDateTime MOCKED_TIME_STAMP = LocalDateTime.of(2020, 2,2, 0, 0);
    private static final String FULL_NAME = "Joe Bloggs";
    private static final String OBJECTOR = "client";

    @Mock
    private ApiLogger apiLogger;

    @Mock
    private ObjectionRepository objectionRepository;

    @Mock
    private Supplier<LocalDateTime> localDateTimeSupplier;

    @Mock
    private ObjectionPatcher objectionPatcher;

    @Mock
    private FileTransferApiClient fileTransferApiClient;

    @Mock
    private ERICHeaderParser ericHeaderParser;

    @Mock
    private ObjectionProcessor objectionProcessor;

    @Mock
    private OracleQueryClient oracleQueryClient;

    @Mock
    private ActionCodeValidator actionCodeValidator;

    @Mock
    private IReferenceNumberGeneratorService referenceNumberGeneratorService;

    @Mock
    private Gaz2RequestedValidator gaz2RequestedValidator;

    @InjectMocks
    private ObjectionService objectionService;

    @Test
    void createObjectionTest() throws ValidationException, ServiceException {
        Objection returnedEntity = new Objection.Builder()
                .withCompanyNumber(COMPANY_NUMBER)
                .build();
        returnedEntity.setId(OBJECTION_ID);
        returnedEntity.setCreatedOn(MOCKED_TIME_STAMP);
        returnedEntity.setStatus(OPEN);

        CreatedBy createdBy = new CreatedBy(AUTH_ID, E_MAIL, OBJECTOR, FULL_NAME,false);
        returnedEntity.setCreatedBy(createdBy);

        when(objectionRepository.insert(any(Objection.class))).thenReturn(returnedEntity);
        when(localDateTimeSupplier.get()).thenReturn(MOCKED_TIME_STAMP);
        when(ericHeaderParser.getEmailAddress(AUTH_USER)).thenReturn(E_MAIL);
        when(oracleQueryClient.getCompanyActionCode(COMPANY_NUMBER, REQUEST_ID)).thenReturn(ACTION_CODE_OK);
        when(referenceNumberGeneratorService.generateReferenceNumber()).thenReturn(OBJECTION_ID);

        Objection objectionResponse =
                objectionService.createObjection(REQUEST_ID, COMPANY_NUMBER, AUTH_ID, AUTH_USER,
                        Utils.buildTestObjectionCreate(OBJECTOR, FULL_NAME, false));

        verify(objectionRepository).insert(any(Objection.class));
        verify(oracleQueryClient).getCompanyActionCode(COMPANY_NUMBER, REQUEST_ID);
        verify(actionCodeValidator).validate(ACTION_CODE_OK, REQUEST_ID);
        verify(gaz2RequestedValidator, times(1)).validate(COMPANY_NUMBER, ACTION_CODE_OK, REQUEST_ID);

        ArgumentCaptor<Objection> saveObjectionCaptor = ArgumentCaptor.forClass(Objection.class);
        verify(objectionRepository, times(1)).insert(saveObjectionCaptor.capture());

        Objection savedObjection = saveObjectionCaptor.getValue();
        assertEquals(COMPANY_NUMBER, savedObjection.getCompanyNumber());
        assertEquals(MOCKED_TIME_STAMP, savedObjection.getCreatedOn());
        assertEquals(AUTH_ID, savedObjection.getCreatedBy().getId());
        assertEquals(E_MAIL, savedObjection.getCreatedBy().getEmail());
        assertEquals(REQUEST_ID, savedObjection.getHttpRequestId());
        assertEquals(ACTION_CODE_OK, savedObjection.getActionCode());
        assertEquals(OPEN, savedObjection.getStatus());
        assertEquals("/company/" + COMPANY_NUMBER + "/strike-off-objections/" + OBJECTION_ID, savedObjection.getLinks().getLink(ObjectionsLinkKeys.SELF));

        assertEquals(OBJECTION_ID, objectionResponse.getId());
        assertEquals(MOCKED_TIME_STAMP, objectionResponse.getCreatedOn());
        assertEquals(COMPANY_NUMBER, objectionResponse.getCompanyNumber());
        assertEquals(OPEN, objectionResponse.getStatus());
        assertEquals(AUTH_ID, objectionResponse.getCreatedBy().getId());
        assertEquals(E_MAIL, objectionResponse.getCreatedBy().getEmail());
    }

    @Test
    void createObjectionIneligibleStatusTest() throws ValidationException, ServiceException {
        when(localDateTimeSupplier.get()).thenReturn(MOCKED_TIME_STAMP);
        when(ericHeaderParser.getEmailAddress(AUTH_USER)).thenReturn(E_MAIL);
        when(oracleQueryClient.getCompanyActionCode(COMPANY_NUMBER, REQUEST_ID)).thenReturn(ACTION_CODE_INELIGIBLE);

        ValidationException ve = new ValidationException(EligibilityStatus.INELIGIBLE_COMPANY_STRUCK_OFF);
        doThrow(ve).when(actionCodeValidator).validate(ACTION_CODE_INELIGIBLE, REQUEST_ID);

        objectionService.createObjection(REQUEST_ID, COMPANY_NUMBER, AUTH_ID, AUTH_USER,
                Utils.buildTestObjectionCreate(OBJECTOR, FULL_NAME, false));

        ArgumentCaptor<Objection> saveObjectionCaptor = ArgumentCaptor.forClass(Objection.class);
        verify(objectionRepository, times(1)).insert(saveObjectionCaptor.capture());

        Objection savedObjection = saveObjectionCaptor.getValue();
        assertEquals(COMPANY_NUMBER, savedObjection.getCompanyNumber());
        assertEquals(MOCKED_TIME_STAMP, savedObjection.getCreatedOn());
        assertEquals(AUTH_ID, savedObjection.getCreatedBy().getId());
        assertEquals(E_MAIL, savedObjection.getCreatedBy().getEmail());
        assertEquals(REQUEST_ID, savedObjection.getHttpRequestId());
        assertEquals(ACTION_CODE_INELIGIBLE, savedObjection.getActionCode());
        assertEquals(INELIGIBLE_COMPANY_STRUCK_OFF, savedObjection.getStatus());
    }

    @Test
    void createObjectionActionCodeOkGaz2Requested() throws ValidationException, ServiceException {
        when(localDateTimeSupplier.get()).thenReturn(MOCKED_TIME_STAMP);
        when(ericHeaderParser.getEmailAddress(AUTH_USER)).thenReturn(E_MAIL);
        when(oracleQueryClient.getCompanyActionCode(COMPANY_NUMBER, REQUEST_ID)).thenReturn(ACTION_CODE_OK);

        ValidationException ve = new ValidationException(EligibilityStatus.INELIGIBLE_COMPANY_STRUCK_OFF);
        doThrow(ve).when(gaz2RequestedValidator).validate(COMPANY_NUMBER, ACTION_CODE_OK, REQUEST_ID);

        objectionService.createObjection(REQUEST_ID, COMPANY_NUMBER, AUTH_ID, AUTH_USER,
                Utils.buildTestObjectionCreate(OBJECTOR, FULL_NAME, false));

        ArgumentCaptor<Objection> saveObjectionCaptor = ArgumentCaptor.forClass(Objection.class);
        verify(objectionRepository, times(1)).insert(saveObjectionCaptor.capture());
        verify(gaz2RequestedValidator, times(1)).validate(COMPANY_NUMBER, ACTION_CODE_OK, REQUEST_ID);

        Objection savedObjection = saveObjectionCaptor.getValue();
        assertEquals(COMPANY_NUMBER, savedObjection.getCompanyNumber());
        assertEquals(MOCKED_TIME_STAMP, savedObjection.getCreatedOn());
        assertEquals(AUTH_ID, savedObjection.getCreatedBy().getId());
        assertEquals(E_MAIL, savedObjection.getCreatedBy().getEmail());
        assertEquals(REQUEST_ID, savedObjection.getHttpRequestId());
        assertEquals(ACTION_CODE_OK, savedObjection.getActionCode());
        assertEquals(INELIGIBLE_COMPANY_STRUCK_OFF, savedObjection.getStatus());
    }

    @Test
    void createObjectionThrowServiceExceptionIfIdExists () {
        when(objectionRepository.insert(any(Objection.class))).thenThrow(new DuplicateKeyException("Duplicate"));

        assertThrows(ServiceException.class, () -> objectionService.createObjection(REQUEST_ID, COMPANY_NUMBER, AUTH_ID, AUTH_USER,
                Utils.buildTestObjectionCreate(OBJECTOR, FULL_NAME, true)));
    }

    @Test
    void patchObjectionExistsTest() throws Exception {
        Objection existingObjection = new Objection();
        existingObjection.setId(OBJECTION_ID);
        Objection objection = new Objection();
        objection.setId(OBJECTION_ID);
        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setFullName(FULL_NAME);
        objectionPatch.setShareIdentity(Boolean.TRUE);
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(ObjectionStatus.PROCESSED);
        when(objectionRepository.findById(any())).thenReturn(Optional.of(existingObjection));
        when(objectionPatcher.patchObjection(any(), any(), any())).thenReturn(objection);

        objectionService.patchObjection(OBJECTION_ID, objectionPatch, REQUEST_ID, COMPANY_NUMBER);

        verify(objectionRepository, times(1)).save(objection);
    }

    @Test
    void patchObjectionDoesNotExistTest() {
        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(OPEN);
        when(objectionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ObjectionNotFoundException.class,
                () -> objectionService.patchObjection( OBJECTION_ID, objectionPatch, REQUEST_ID, COMPANY_NUMBER));

        verify(objectionRepository, times(0)).save(any());
    }

    @Test
    void patchObjectionSubmittedTest() throws Exception {
        Objection existingObjection = new Objection();
        existingObjection.setId(OBJECTION_ID);
        existingObjection.setStatus(OPEN);

        Objection objection = new Objection();
        objection.setId(OBJECTION_ID);

        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setFullName(FULL_NAME);
        objectionPatch.setShareIdentity(Boolean.TRUE);
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(ObjectionStatus.SUBMITTED);

        when(objectionRepository.findById(any())).thenReturn(Optional.of(existingObjection));
        when(objectionPatcher.patchObjection(any(), any(), any())).thenReturn(objection);

        objectionService.patchObjection( OBJECTION_ID, objectionPatch, REQUEST_ID, COMPANY_NUMBER);

        verify(objectionRepository, times(1)).save(objection);
        verify(objectionProcessor, only()).process(objection, REQUEST_ID);
    }

    @Test
    void patchObjectionPropagatesProcessInvalidStatusExceptionTest() throws Exception {
        Objection existingObjection = new Objection();
        existingObjection.setId(OBJECTION_ID);
        existingObjection.setStatus(OPEN);

        Objection objection = new Objection();
        objection.setId(OBJECTION_ID);

        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setFullName(FULL_NAME);
        objectionPatch.setShareIdentity(Boolean.TRUE);
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(ObjectionStatus.SUBMITTED);

        when(objectionRepository.findById(any())).thenReturn(Optional.of(existingObjection));
        when(objectionPatcher.patchObjection(any(), any(), any())).thenReturn(objection);
        doThrow(new InvalidObjectionStatusException("Invalid")).when(objectionProcessor).process(any(), any());

        assertThrows(InvalidObjectionStatusException.class,
                () -> objectionService.patchObjection( OBJECTION_ID, objectionPatch, REQUEST_ID, COMPANY_NUMBER));

        verify(objectionRepository, times(1)).save(objection);
        verify(objectionProcessor, only()).process(objection, REQUEST_ID);
    }

    @Test
    void patchObjectionWithNoStatusTest() throws Exception {
        Objection existingObjection = new Objection();
        existingObjection.setId(OBJECTION_ID);

        Objection objection = new Objection();
        objection.setId(OBJECTION_ID);

        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setReason(REASON);

        when(objectionRepository.findById(any())).thenReturn(Optional.of(existingObjection));
        when(objectionPatcher.patchObjection(any(), any(), any())).thenReturn(objection);

        objectionService.patchObjection( OBJECTION_ID, objectionPatch, REQUEST_ID, COMPANY_NUMBER);

        verify(objectionRepository, times(1)).save(objection);
        verifyNoInteractions(objectionProcessor);
    }

    @Test
    void patchObjectionWithIncorrectSubmittedStatusTest() {
        Objection existingObjection = new Objection();
        existingObjection.setId(OBJECTION_ID);
        existingObjection.setStatus(ObjectionStatus.PROCESSED);

        Objection objection = new Objection();
        objection.setId(OBJECTION_ID);

        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(ObjectionStatus.SUBMITTED);

        when(objectionRepository.findById(any())).thenReturn(Optional.of(existingObjection));

        assertThrows(InvalidObjectionStatusException.class,
                () -> objectionService.patchObjection( OBJECTION_ID, objectionPatch, REQUEST_ID, COMPANY_NUMBER));

        verify(objectionRepository, never()).save(objection);
        verifyNoInteractions(objectionPatcher);
        verifyNoInteractions(objectionProcessor);
    }

    @Test
    void getObjectionWhenObjectionExistsTest() throws Exception {
        Objection objection = new Objection();
        objection.setId(OBJECTION_ID);
        when(objectionRepository.findById(any())).thenReturn(Optional.of(objection));

        Objection returnedObjection = objectionService.getObjection(REQUEST_ID, OBJECTION_ID);

        assertEquals(objection, returnedObjection);
        verify(objectionRepository, times(1)).findById(OBJECTION_ID);
    }

    @Test
    void getObjectionWhenObjectionDoesNotExistTest() {
        Objection objection = new Objection();
        objection.setId(OBJECTION_ID);
        when(objectionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ObjectionNotFoundException.class,
                () -> objectionService.getObjection(REQUEST_ID, OBJECTION_ID));

        verify(objectionRepository, times(1)).findById(OBJECTION_ID);
    }

    @Test
    void canAddAnAttachment() throws Exception {
        Objection existingObjection = new Objection();
        existingObjection.setId(OBJECTION_ID);
        when(fileTransferApiClient.upload(anyString(), any(MultipartFile.class)))
                .thenReturn(Utils.getSuccessfulUploadResponse());
        when(objectionRepository.findById(any())).thenReturn(Optional.of(existingObjection));
        ServiceResult<String> attachmentIdResult =
                objectionService.addAttachment(REQUEST_ID, OBJECTION_ID, Utils.mockMultipartFile(), ACCESS_URL);
        assertEquals(Utils.UPLOAD_ID, attachmentIdResult.getData());
        Optional<Attachment> entityAttachment = existingObjection
                .getAttachments()
                .stream()
                .findAny();

        assertTrue(entityAttachment.isPresent());
        String linkUrl = entityAttachment.get().getLinks().getLink(ObjectionsLinkKeys.SELF);
        String downloadUrl = entityAttachment.get().getLinks().getLink(ObjectionsLinkKeys.DOWNLOAD);
        assertEquals(linkUrl + "/download", downloadUrl);
        assertTrue(linkUrl.startsWith(ACCESS_URL));
        assertFalse(linkUrl.endsWith(ACCESS_URL + "/"));
        assertNotNull(entityAttachment.get().getId());

        verify(objectionRepository).save(existingObjection);
        verify(objectionRepository, times(1)).findById(OBJECTION_ID);
    }

    @Test
    void willNotOverrideAlreadyExistingAttachments() throws Exception {
        Objection existingObjection = new Objection();
        existingObjection.setId(OBJECTION_ID);

        when(fileTransferApiClient.upload(anyString(), any(MultipartFile.class)))
                .thenReturn(Utils.getSuccessfulUploadResponse());

        Attachment attachment = new Attachment();
        attachment.setSize(1L);
        attachment.setContentType("text/plain");
        attachment.setName("testFile");
        String newId = "12345a";
        attachment.setId(newId);
        List<Attachment> attachmentsList = new ArrayList<>();
        attachmentsList.add(attachment);
        existingObjection.setAttachments(attachmentsList);

        when(objectionRepository.findById(any())).thenReturn(Optional.of(existingObjection));

        objectionService.addAttachment(
                 REQUEST_ID, OBJECTION_ID, Utils.mockMultipartFile(), ACCESS_URL);

        List<Attachment> objectionAttachments = existingObjection.getAttachments();

        assertEquals(2, objectionAttachments.size());
        assertEquals("testFile", objectionAttachments.get(0).getName());
        assertEquals(Utils.ORIGINAL_FILE_NAME, objectionAttachments.get(1).getName());
    }

    @Test
    void getAttachmentWhenObjectionExistsTest() throws Exception {
        Objection existingObjection = new Objection();
        existingObjection.setId(OBJECTION_ID);
        Attachment attachment = new Attachment();
        existingObjection.addAttachment(attachment);
        when(objectionRepository.findById(any())).thenReturn(Optional.of(existingObjection));

        List<Attachment> attachments = objectionService.getAttachments(REQUEST_ID, COMPANY_NUMBER, OBJECTION_ID);

        assertEquals(1, attachments.size());
        assertEquals(attachment, attachments.getFirst());
    }

    @Test
    void getAttachmentsWhenObjectionDoesNotExistTest() {
        when(objectionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ObjectionNotFoundException.class,
                () -> objectionService.getAttachments(REQUEST_ID, COMPANY_NUMBER, OBJECTION_ID));

        verify(objectionRepository, times(0)).save(any());
    }

    @Test
    void willThrowServiceExceptionIfUploadErrors() throws Exception {
        when(fileTransferApiClient.upload(anyString(), any(MultipartFile.class)))
                .thenReturn(Utils.getUnsuccessfulFileTransferApiResponse());
        try {
            objectionService.addAttachment(REQUEST_ID, OBJECTION_ID, Utils.mockMultipartFile(), ACCESS_URL);
            fail();
        } catch(ServiceException e) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
        }
    }

    @Test
    void willPropagateServerRuntimeExceptions() throws Exception {
        when(fileTransferApiClient.upload(anyString(), any(MultipartFile.class)))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        MultipartFile mockFile = Utils.mockMultipartFile();
        try {
            objectionService.addAttachment(REQUEST_ID, OBJECTION_ID, mockFile, ACCESS_URL);
            fail();
        } catch(HttpServerErrorException e) {
            assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.toString(), e.getMessage());
        }
    }

    @Test
    void willPropagateClientRuntimeExceptions() throws Exception {
        when(fileTransferApiClient.upload(anyString(), any(MultipartFile.class)))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        MultipartFile mockFile = Utils.mockMultipartFile();
        try {
            objectionService.addAttachment(REQUEST_ID, OBJECTION_ID, mockFile, ACCESS_URL);
            fail();
        } catch(HttpClientErrorException e) {
            assertEquals(HttpStatus.BAD_REQUEST.toString(), e.getMessage());
        }
    }

    @Test
    void willThrowServiceExceptions() {
        FileTransferApiClientResponse response = new FileTransferApiClientResponse();
        response.setFileId("");
                when(fileTransferApiClient.upload(anyString(), any(MultipartFile.class)))
                .thenReturn(response);

        assertThrows(ServiceException.class, () ->
                objectionService.addAttachment(REQUEST_ID, OBJECTION_ID, Utils.mockMultipartFile(), ACCESS_URL));

    }

    @Test
    void getAttachmentTest() throws ObjectionNotFoundException, AttachmentNotFoundException {
        Objection existingObjection = new Objection();
        existingObjection.setId(OBJECTION_ID);
        Attachment attachment = new Attachment();
        attachment.setId(ATTACHMENT_ID);
        existingObjection.addAttachment(attachment);
        when(objectionRepository.findById(any())).thenReturn(Optional.of(existingObjection));

        Attachment returnedAttachment = objectionService.getAttachment(
                REQUEST_ID,
                COMPANY_NUMBER,
                OBJECTION_ID,
                ATTACHMENT_ID
        );

        assertEquals(attachment, returnedAttachment);
    }

    @Test
    void getAttachmentTestWhenObjectionDoesNotExist() {

        when(objectionRepository.findById(any())).thenReturn(Optional.empty());

        assertThrows(ObjectionNotFoundException.class, () -> objectionService.getAttachment(
                REQUEST_ID,
                COMPANY_NUMBER,
                OBJECTION_ID,
                ATTACHMENT_ID
                )
        );
    }

    @Test
    void getAttachmentTestAttachmentDoesNotExist() {

        Objection objection = new Objection();
        when(objectionRepository.findById(any())).thenReturn(Optional.of(objection));

        assertThrows(AttachmentNotFoundException.class, () -> objectionService.getAttachment(
                REQUEST_ID,
                COMPANY_NUMBER,
                OBJECTION_ID,
                ATTACHMENT_ID
                )
        );
    }

    @Test
    void deleteAttachmentTest() throws ObjectionNotFoundException, AttachmentNotFoundException, ServiceException {
        Objection existingObjection = new Objection();
        existingObjection.setId(OBJECTION_ID);
        Attachment attachment = new Attachment();
        attachment.setId(ATTACHMENT_ID);
        existingObjection.addAttachment(attachment);
        when(objectionRepository.findById(any())).thenReturn(Optional.of(existingObjection));
        when(fileTransferApiClient.delete(REQUEST_ID, ATTACHMENT_ID)).thenReturn(Utils.getSuccessfulDeleteResponse());
        objectionService.deleteAttachment(
                REQUEST_ID,
                OBJECTION_ID,
                ATTACHMENT_ID
        );

        verify(objectionRepository, times(1)).save(existingObjection);
        verify(fileTransferApiClient, times(1)).delete(REQUEST_ID, ATTACHMENT_ID);
        assertFalse(existingObjection.getAttachments().contains(attachment));
    }

    @Test
    void deleteAttachmentTestWhenObjectionDoesNotExist() {

        when(objectionRepository.findById(any())).thenReturn(Optional.empty());
        assertThrows(ObjectionNotFoundException.class, () -> objectionService.deleteAttachment(
                REQUEST_ID,
                OBJECTION_ID,
                ATTACHMENT_ID)
        );
    }

    @Test
    void deleteAttachmentTestAttachmentDoesNotExist() {
        Objection objection = new Objection();
        when(objectionRepository.findById(any())).thenReturn(Optional.of(objection));
        assertThrows(AttachmentNotFoundException.class, () -> objectionService.deleteAttachment(
                REQUEST_ID,
                OBJECTION_ID,
                ATTACHMENT_ID)
        );
    }

    @Test
    void deleteAttachmentHandleClientExceptionFromS3() {
        Objection objection = Utils.getSimpleTestObjection(OBJECTION_ID);
        Utils.getTestAttachmentsContainingKey(ATTACHMENT_ID).forEach(objection::addAttachment);
        HttpServerErrorException clientException = new HttpServerErrorException(HttpStatus.BAD_REQUEST);
        when(fileTransferApiClient.delete(REQUEST_ID, ATTACHMENT_ID)).thenThrow(clientException);
        when(objectionRepository.findById(objection.getId()))
                .thenReturn(Optional.of(objection));

        assertThrows(ServiceException.class, () -> objectionService.deleteAttachment(
                REQUEST_ID,
                OBJECTION_ID,
                ATTACHMENT_ID
            )
        );

        verify(objectionRepository, never()).save(objection);
        verify(fileTransferApiClient, times(1)).delete(REQUEST_ID, ATTACHMENT_ID);
        verify(apiLogger).errorContext(
                eq(REQUEST_ID),
                eq(String.format("Unable to delete attachment %s, status code 400 BAD_REQUEST", ATTACHMENT_ID)),
                eq(clientException),
                any());
    }

    @Test
    void deleteAttachmentHandleServiceExceptionFromS3() {
        Objection objection = Utils.getSimpleTestObjection(OBJECTION_ID);
        Utils.getTestAttachmentsContainingKey(ATTACHMENT_ID).forEach(objection::addAttachment);

        HttpServerErrorException serviceException = new HttpServerErrorException(HttpStatus.GATEWAY_TIMEOUT);
        when(fileTransferApiClient.delete(REQUEST_ID, ATTACHMENT_ID)).thenThrow(serviceException);
        when(objectionRepository.findById(objection.getId()))
                .thenReturn(Optional.of(objection));

        assertThrows(ServiceException.class, () -> objectionService.deleteAttachment(
                REQUEST_ID,
                OBJECTION_ID,
                ATTACHMENT_ID
            )
        );
        verify(objectionRepository, never()).save(objection);
        verify(fileTransferApiClient, times(1)).delete(REQUEST_ID, ATTACHMENT_ID);
        verify(apiLogger).errorContext(
                eq(REQUEST_ID),
                eq(String.format("Unable to delete attachment %s, status code 504 GATEWAY_TIMEOUT", ATTACHMENT_ID)),
                eq(serviceException),
                any());
    }

    @Test
    void deleteAttachmentHandleHttpErrorCodeInFileTransferResponse() {
        Objection objection = Utils.getSimpleTestObjection(OBJECTION_ID);
        Utils.getTestAttachmentsContainingKey(ATTACHMENT_ID).forEach(objection::addAttachment);

        when(fileTransferApiClient.delete(REQUEST_ID, ATTACHMENT_ID))
                .thenReturn(Utils.getUnsuccessfulFileTransferApiResponse());




        when(objectionRepository.findById(objection.getId()))
                .thenReturn(Optional.of(objection));

        assertThrows(ServiceException.class, () -> objectionService.deleteAttachment(
                REQUEST_ID,
                OBJECTION_ID,
                ATTACHMENT_ID
            )
        );

        verify(objectionRepository, never()).save(objection);
        verify(fileTransferApiClient, times(1)).delete(REQUEST_ID, ATTACHMENT_ID);
        verify(apiLogger).infoContext(
                eq(REQUEST_ID),
                eq(String.format("Unable to delete attachment %s, status code 500 INTERNAL_SERVER_ERROR", ATTACHMENT_ID)),
                any());
    }

    @Test
    void deleteAttachmentHandleNullApiResponseOnDeleteAttachment() {
        Objection objection = Utils.getSimpleTestObjection(OBJECTION_ID);
        Utils.getTestAttachmentsContainingKey(ATTACHMENT_ID).forEach(objection::addAttachment);
        when(fileTransferApiClient.delete(REQUEST_ID, ATTACHMENT_ID)).thenReturn(null);

        when(objectionRepository.findById(objection.getId()))
                .thenReturn(Optional.of(objection));

        assertThrows(ServiceException.class, () -> objectionService.deleteAttachment(
                REQUEST_ID,
                OBJECTION_ID,
                ATTACHMENT_ID
            )
        );

        verify(objectionRepository, never()).save(objection);
        verify(fileTransferApiClient, times(1)).delete(REQUEST_ID, ATTACHMENT_ID);
        verify(apiLogger).infoContext(
                eq(REQUEST_ID),
                eq(String.format("Unable to delete attachment %s", ATTACHMENT_ID)),
                any());
    }

    @Test
    void deleteAttachmentHandleNullHttpStatusApiResponseOnDeleteAttachment() {
        Objection objection = Utils.getSimpleTestObjection(OBJECTION_ID);
        Utils.getTestAttachmentsContainingKey(ATTACHMENT_ID).forEach(objection::addAttachment);
        FileTransferApiClientResponse response = new FileTransferApiClientResponse();
        response.setHttpStatus(null);
        when(fileTransferApiClient.delete(REQUEST_ID, ATTACHMENT_ID)).thenReturn(response);

        when(objectionRepository.findById(objection.getId()))
                .thenReturn(Optional.of(objection));

        assertThrows(ServiceException.class, () -> objectionService.deleteAttachment(
                REQUEST_ID,
                OBJECTION_ID,
                ATTACHMENT_ID
            )
        );

        verify(objectionRepository, never()).save(objection);
        verify(fileTransferApiClient, times(1)).delete(REQUEST_ID, ATTACHMENT_ID);
        verify(apiLogger).infoContext(
                eq(REQUEST_ID),
                eq(String.format("Unable to delete attachment %s", ATTACHMENT_ID)),
                any());
    }

    @Test
    void willCallFileTransferApiForDownload() {
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        FileTransferApiClientResponse dummyDownloadResponse = Utils.dummyDownloadResponse();

        when(fileTransferApiClient.download(REQUEST_ID, ATTACHMENT_ID, httpServletResponse)).thenReturn(dummyDownloadResponse);

        FileTransferApiClientResponse downloadServiceResult = objectionService.downloadAttachment(
                REQUEST_ID, OBJECTION_ID, ATTACHMENT_ID, httpServletResponse);

        verify(fileTransferApiClient, only()).download(REQUEST_ID, ATTACHMENT_ID, httpServletResponse);
        verify(fileTransferApiClient, times(1)).download(REQUEST_ID, ATTACHMENT_ID, httpServletResponse);

        assertNotNull(downloadServiceResult);
        assertEquals(HttpStatus.OK, downloadServiceResult.getHttpStatus());
    }

    @Test
    void willReturnTrueEligibilityResponseWhenActionCodeOk() throws ValidationException {

        when(oracleQueryClient.getCompanyActionCode(COMPANY_NUMBER, REQUEST_ID)).thenReturn(ACTION_CODE_OK);
        ObjectionEligibility response = objectionService.isCompanyEligible(COMPANY_NUMBER, REQUEST_ID);

        verify(oracleQueryClient).getCompanyActionCode(COMPANY_NUMBER, REQUEST_ID);
        verify(actionCodeValidator).validate(ACTION_CODE_OK, REQUEST_ID);
        verify(gaz2RequestedValidator, times(1)).validate(COMPANY_NUMBER, ACTION_CODE_OK, REQUEST_ID);

        assertTrue(response.isEligible());
        assertEquals(EligibilityStatus.ELIGIBLE, response.getEligibilityStatus());
    }

    @Test
    void willReturnFalseEligibilityResponseWhenActionCodeIneligible() throws ValidationException {

        when(oracleQueryClient.getCompanyActionCode(COMPANY_NUMBER, REQUEST_ID)).thenReturn(ACTION_CODE_INELIGIBLE);
        doThrow(new ValidationException(EligibilityStatus.INELIGIBLE_COMPANY_STRUCK_OFF)).when(actionCodeValidator).validate(ACTION_CODE_INELIGIBLE, REQUEST_ID);
        ObjectionEligibility response = objectionService.isCompanyEligible(COMPANY_NUMBER, REQUEST_ID);

        verify(oracleQueryClient).getCompanyActionCode(COMPANY_NUMBER, REQUEST_ID);
        verify(actionCodeValidator).validate(ACTION_CODE_INELIGIBLE, REQUEST_ID);
        verify(gaz2RequestedValidator, times(0)).validate(COMPANY_NUMBER, ACTION_CODE_INELIGIBLE, REQUEST_ID);

        assertFalse(response.isEligible());
        assertEquals(EligibilityStatus.INELIGIBLE_COMPANY_STRUCK_OFF, response.getEligibilityStatus());
    }

    @Test
    void willReturnCorrectEligibilityStatusWhenGaz2Requested() throws ValidationException {

        when(oracleQueryClient.getCompanyActionCode(COMPANY_NUMBER, REQUEST_ID)).thenReturn(ACTION_CODE_OK);
        doThrow(new ValidationException(EligibilityStatus.INELIGIBLE_GAZ2_REQUESTED)).when(gaz2RequestedValidator)
                .validate(COMPANY_NUMBER, ACTION_CODE_OK, REQUEST_ID);
        ObjectionEligibility response = objectionService.isCompanyEligible(COMPANY_NUMBER, REQUEST_ID);

        verify(oracleQueryClient).getCompanyActionCode(COMPANY_NUMBER, REQUEST_ID);
        verify(actionCodeValidator).validate(ACTION_CODE_OK, REQUEST_ID);
        verify(gaz2RequestedValidator, times(1)).validate(COMPANY_NUMBER, ACTION_CODE_OK, REQUEST_ID);

        assertFalse(response.isEligible());
        assertEquals(EligibilityStatus.INELIGIBLE_GAZ2_REQUESTED, response.getEligibilityStatus());
    }
}
