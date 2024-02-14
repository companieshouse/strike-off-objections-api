package uk.gov.companieshouse.api.strikeoffobjections.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.LogConstants;
import uk.gov.companieshouse.api.strikeoffobjections.exception.AttachmentNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.exception.InvalidObjectionStatusException;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.create.ObjectionCreate;
import uk.gov.companieshouse.api.strikeoffobjections.model.eligibility.EligibilityStatus;
import uk.gov.companieshouse.api.strikeoffobjections.model.eligibility.ObjectionEligibility;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;
import uk.gov.companieshouse.api.strikeoffobjections.model.response.AttachmentResponseDTO;
import uk.gov.companieshouse.api.strikeoffobjections.model.response.CreatedByResponseDTO;
import uk.gov.companieshouse.api.strikeoffobjections.model.response.ObjectionResponseDTO;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.links.CoreLinkKeys;
import uk.gov.companieshouse.service.links.Links;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

@Unit
@ExtendWith(MockitoExtension.class)
class ObjectionControllerTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String REQUEST_ID = "87654321";
    private static final String AUTH_ID = "22334455";
    private static final String AUTH_USER =
            "demo@ch.gov.uk; forename=demoForename; surname=demoSurname";
    private static final String OBJECTION_ID = "87651234";
    private static final String ATTACHMENT_ID = "12348765";
    private static final String REASON = "REASON";
    private static final String ACCESS_URL = "/dummyUrl";
    private static final String FULL_NAME = "Joe Bloggs";
    private static final String ATTACHMENT_NAME = "name";
    private static final String ATTACHMENT_CONTENT = "Content";
    private static final long ATTACHMENT_SIZE = 12L;
    private static final String OBJECTOR = "client";

    @Mock private IObjectionService objectionService;

    @Mock private ApiLogger apiLogger;

    @Mock private PluggableResponseEntityFactory pluggableResponseEntityFactory;

    @Mock private ObjectionMapper objectionMapper;

    @Mock private AttachmentMapper attachmentMapper;

    @Mock private HttpServletRequest servletRequest;

    @InjectMocks private ObjectionController objectionController;

    @Test
    void createObjectionTest() throws ServiceException {
        ObjectionCreate objectionCreate = new ObjectionCreate();
        objectionCreate.setObjector(OBJECTOR);
        objectionCreate.setFullName(FULL_NAME);
        objectionCreate.setShareIdentity(false);

        Objection objection = new Objection();
        objection.setId(OBJECTION_ID);
        objection.setStatus(ObjectionStatus.OPEN);

        when(objectionService.createObjection(
                        REQUEST_ID, COMPANY_NUMBER, AUTH_ID, AUTH_USER, objectionCreate))
                .thenReturn(objection);

        // this is to copy the param used in the ServiceResult into the response factory
        when(pluggableResponseEntityFactory.createResponse(any()))
                .then(
                        invocation -> {
                            ServiceResult serviceResult = invocation.getArgument(0, ServiceResult.class);
                            return ResponseEntity.status(HttpStatus.CREATED)
                                    .body(ChResponseBody.createNormalBody(serviceResult.getData()));
                        });

        ResponseEntity<ChResponseBody<ObjectionResponseDTO>> response =
                objectionController.createObjection(
                        COMPANY_NUMBER, REQUEST_ID, AUTH_ID, AUTH_USER, objectionCreate);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        assertNotNull(response.getBody());
        ChResponseBody<ObjectionResponseDTO> responseBody = response.getBody();

        assertNotNull(responseBody.getSuccessBody());
        assertEquals(OBJECTION_ID, responseBody.getSuccessBody().getId());
        assertEquals(ObjectionStatus.OPEN, responseBody.getSuccessBody().getStatus());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logOrder
                .verify(apiLogger)
                .infoContext(eq(REQUEST_ID), contains("Processing POST / request"), eq(logMap));
        logOrder
                .verify(apiLogger)
                .infoContext(eq(REQUEST_ID), contains("Successfully processed POST / request"), eq(logMap));
        ;
    }

    @Test
    void createObjectionIneligibleTest() throws ServiceException {
        ObjectionCreate objectionCreate = new ObjectionCreate();
        objectionCreate.setObjector(OBJECTOR);
        objectionCreate.setFullName(FULL_NAME);
        objectionCreate.setShareIdentity(false);

        Objection objection = new Objection();
        objection.setId(OBJECTION_ID);
        objection.setStatus(ObjectionStatus.INELIGIBLE_COMPANY_STRUCK_OFF);

        when(objectionService.createObjection(
                        REQUEST_ID, COMPANY_NUMBER, AUTH_ID, AUTH_USER, objectionCreate))
                .thenReturn(objection);

        // this is to copy the param used in the ServiceResult into the response factory
        when(pluggableResponseEntityFactory.createResponse(any()))
                .then(
                        invocation -> {
                            ServiceResult serviceResult = invocation.getArgument(0, ServiceResult.class);
                            return ResponseEntity.status(HttpStatus.CREATED)
                                    .body(ChResponseBody.createNormalBody(serviceResult.getData()));
                        });

        ResponseEntity<ChResponseBody<ObjectionResponseDTO>> response =
                objectionController.createObjection(
                        COMPANY_NUMBER, REQUEST_ID, AUTH_ID, AUTH_USER, objectionCreate);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertNotNull(response.getBody());
        ChResponseBody<ObjectionResponseDTO> responseBody = response.getBody();
        assertNotNull(responseBody.getSuccessBody());
        assertEquals(OBJECTION_ID, responseBody.getSuccessBody().getId());
        assertEquals(
                ObjectionStatus.INELIGIBLE_COMPANY_STRUCK_OFF, responseBody.getSuccessBody().getStatus());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logOrder
                .verify(apiLogger)
                .infoContext(eq(REQUEST_ID), contains("Processing POST / request"), eq(logMap));
        logOrder
                .verify(apiLogger)
                .infoContext(eq(REQUEST_ID), contains("Successfully processed POST / request"), eq(logMap));
    }

    @Test
    void createObjectionExceptionTest() throws ServiceException {
        ObjectionCreate objectionCreate = new ObjectionCreate();
        objectionCreate.setObjector(OBJECTOR);
        objectionCreate.setFullName(FULL_NAME);
        objectionCreate.setShareIdentity(false);
        when(objectionService.createObjection(
                        REQUEST_ID, COMPANY_NUMBER, AUTH_ID, AUTH_USER, objectionCreate))
                .thenThrow(new RuntimeException("ERROR MESSAGE"));
        when(pluggableResponseEntityFactory.createEmptyInternalServerError())
                .thenReturn(ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build());

        ResponseEntity<ChResponseBody<ObjectionResponseDTO>> response =
                objectionController.createObjection(
                        COMPANY_NUMBER, REQUEST_ID, AUTH_ID, AUTH_USER, objectionCreate);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logOrder
                .verify(apiLogger)
                .infoContext(eq(REQUEST_ID), contains("Processing POST / request"), eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(
                        eq(REQUEST_ID), contains("Error creating the Strike-Off Objection"), any(), eq(logMap));
    }

    @Test
    void patchObjectionTest() {
        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(ObjectionStatus.OPEN);
        ResponseEntity response =
                objectionController.patchObjection(
                        COMPANY_NUMBER, OBJECTION_ID, objectionPatch, REQUEST_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID), contains("Processing PATCH /{objectionId} request"), eq(logMap));
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Successfully processed PATCH /{objectionId} request"),
                        eq(logMap));
    }

    @Test
    void patchObjectionNotFoundExceptionTest() throws Exception {
        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(ObjectionStatus.OPEN);
        doThrow(new ObjectionNotFoundException("Message"))
                .when(objectionService)
                .patchObjection(any(), any(), any(), any());
        ResponseEntity response =
                objectionController.patchObjection(
                        COMPANY_NUMBER, OBJECTION_ID, objectionPatch, REQUEST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID), contains("Processing PATCH /{objectionId} request"), eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(eq(REQUEST_ID), contains("Objection not found"), any(), eq(logMap));
    }

    @Test
    void patchObjectionSubmittedInvalidObjectionStatusExceptionTest()
            throws InvalidObjectionStatusException, ObjectionNotFoundException, ServiceException {
        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(ObjectionStatus.SUBMITTED);

        doThrow(new InvalidObjectionStatusException("Message"))
                .when(objectionService)
                .patchObjection(any(), any(), any(), any());

        ResponseEntity response =
                objectionController.patchObjection(
                        COMPANY_NUMBER, OBJECTION_ID, objectionPatch, REQUEST_ID);

        assertEquals(HttpStatus.UNPROCESSABLE_ENTITY, response.getStatusCode());
        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID), contains("Processing PATCH /{objectionId} request"), eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(eq(REQUEST_ID), contains("Objection not processed"), any(), eq(logMap));
    }

    @Test
    void patchObjectionGenericExceptionTest()
            throws ObjectionNotFoundException, InvalidObjectionStatusException, ServiceException {
        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(ObjectionStatus.SUBMITTED);
        doThrow(new RuntimeException("Message"))
                .when(objectionService)
                .patchObjection(any(), any(), any(), any());

        ResponseEntity response =
                objectionController.patchObjection(
                        COMPANY_NUMBER, OBJECTION_ID, objectionPatch, REQUEST_ID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID), contains("Processing PATCH /{objectionId} request"), eq(logMap));
        logOrder.verify(apiLogger).errorContext(eq(REQUEST_ID), contains("Message"), any(), eq(logMap));
    }

    @Test
    void patchObjectionNullTest() {
        ResponseEntity response =
                objectionController.patchObjection(COMPANY_NUMBER, OBJECTION_ID, null, REQUEST_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID), contains("Processing PATCH /{objectionId} request"), eq(logMap));
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Successfully processed PATCH /{objectionId} request"),
                        eq(logMap));
    }

    @Test
    void getObjectionTest() throws Exception {
        Objection objection = new Objection();
        objection.setId(OBJECTION_ID);
        objection.setStatus(ObjectionStatus.OPEN);
        ObjectionResponseDTO objectionDTO = new ObjectionResponseDTO();
        objectionDTO.setId(OBJECTION_ID);
        objectionDTO.setStatus(ObjectionStatus.OPEN);
        when(objectionMapper.objectionEntityToObjectionResponseDTO(objection)).thenReturn(objectionDTO);
        when(objectionService.getObjection(any(), any())).thenReturn(objection);
        when(pluggableResponseEntityFactory.createResponse(any()))
                .then(
                        invocation -> {
                            ServiceResult serviceResult = invocation.getArgument(0, ServiceResult.class);
                            return ResponseEntity.status(HttpStatus.OK)
                                    .body(ChResponseBody.createNormalBody(serviceResult.getData()));
                        });

        ResponseEntity<ChResponseBody<ObjectionResponseDTO>> response =
                objectionController.getObjection(COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID);
        ChResponseBody<ObjectionResponseDTO> responseBody = response.getBody();

        assertNotNull(responseBody);
        ObjectionResponseDTO responseDTO = responseBody.getSuccessBody();

        verify(objectionMapper, times(1)).objectionEntityToObjectionResponseDTO(objection);

        assertEquals(objection.getId(), responseDTO.getId());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(eq(REQUEST_ID), contains("Processing GET /{objectionId} request"), eq(logMap));
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Successfully processed GET /{objectionId} request"),
                        eq(logMap));
    }

    @Test
    void getObjectionObjectionNotFoundTest() throws Exception {
        doThrow(new ObjectionNotFoundException("Message"))
                .when(objectionService)
                .getObjection(any(), any());
        ResponseEntity<ChResponseBody<ObjectionResponseDTO>> response =
                objectionController.getObjection(COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(eq(REQUEST_ID), contains("Processing GET /{objectionId} request"), eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(eq(REQUEST_ID), contains("Objection not found"), any(), eq(logMap));
    }

    @Test
    void getObjectionUnexpectedExceptionThrownTest() throws Exception {
        doThrow(new RuntimeException()).when(objectionService).getObjection(any(), any());
        ResponseEntity<ChResponseBody<ObjectionResponseDTO>> response =
                objectionController.getObjection(COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(eq(REQUEST_ID), contains("Processing GET /{objectionId} request"), eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(eq(REQUEST_ID), contains("Internal server error"), any(), eq(logMap));
    }

    @Test
    void getAttachmentsTest() throws Exception {
        Links links = new Links();
        List<Attachment> attachments = new ArrayList<>();
        Attachment attachment = new Attachment();
        attachment.setId(ATTACHMENT_ID);
        attachment.setName(ATTACHMENT_NAME);
        attachment.setContentType(ATTACHMENT_CONTENT);
        attachment.setLinks(links);
        attachment.setSize(ATTACHMENT_SIZE);
        attachments.add(attachment);

        AttachmentResponseDTO attachmentResponseDTO = new AttachmentResponseDTO();
        attachmentResponseDTO.setId(ATTACHMENT_ID);
        attachmentResponseDTO.setName(ATTACHMENT_NAME);
        attachmentResponseDTO.setContentType(ATTACHMENT_CONTENT);
        attachmentResponseDTO.setLinks(links);
        attachmentResponseDTO.setSize(ATTACHMENT_SIZE);

        when(objectionService.getAttachments(REQUEST_ID, COMPANY_NUMBER, OBJECTION_ID))
                .thenReturn(attachments);
        when(attachmentMapper.attachmentEntityToAttachmentResponseDTO(attachment))
                .thenReturn(attachmentResponseDTO);

        when(pluggableResponseEntityFactory.createResponse(any()))
                .then(
                        invocation -> {
                            ServiceResult serviceResult = invocation.getArgument(0, ServiceResult.class);
                            return ResponseEntity.status(HttpStatus.FOUND)
                                    .body(ChResponseBody.createNormalBody(serviceResult.getData()));
                        });

        ResponseEntity<ChResponseBody<List<AttachmentResponseDTO>>> response =
                objectionController.getAttachments(COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID);

        verify(attachmentMapper, times(1)).attachmentEntityToAttachmentResponseDTO(attachment);

        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        InOrder logOrder = inOrder(apiLogger);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing GET /{objectionId}/attachments request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Successfully processed GET /{objectionId}/attachments request"),
                        eq(logMap));

        assertEquals(HttpStatus.FOUND, response.getStatusCode());

        assertNotNull(response.getBody());
        ChResponseBody<List<AttachmentResponseDTO>> responseBody = response.getBody();

        assertNotNull(responseBody.getSuccessBody());
        assertEquals(1, responseBody.getSuccessBody().size());

        AttachmentResponseDTO returnedAttachmentResponseDTO = responseBody.getSuccessBody().getFirst();
        assertEquals(attachmentResponseDTO, returnedAttachmentResponseDTO);
    }

    @Test
    void getAttachmentsObjectionNotFoundExceptionTest() throws Exception {
        doThrow(new ObjectionNotFoundException("Message"))
                .when(objectionService)
                .getAttachments(any(), any(), any());
        ResponseEntity<ChResponseBody<List<AttachmentResponseDTO>>> response =
                objectionController.getAttachments(COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        InOrder logOrder = inOrder(apiLogger);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing GET /{objectionId}/attachments request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(eq(REQUEST_ID), contains("Objection not found"), any(), eq(logMap));
    }

    @Test
    void testAttachmentMapper() {
        // given
        Attachment attachment = new Attachment();
        attachment.setId("123-456");
        attachment.setName("xyz");
        attachment.setSize(5);
        attachment.setContentType("TEXT");
        Links links = new Links();
        links.setLink(CoreLinkKeys.SELF, "link to SELF");
        attachment.setLinks(links);

        // when
        AttachmentMapper mapper = Mappers.getMapper(AttachmentMapper.class);

        AttachmentResponseDTO attachmentDTO =
                mapper.attachmentEntityToAttachmentResponseDTO(attachment);

        // then
        assertNotNull(attachmentDTO);
        assertEquals("123-456", attachmentDTO.getId());
        assertEquals("xyz", attachmentDTO.getName());
        assertEquals("TEXT", attachmentDTO.getContentType());
        assertEquals(5, attachmentDTO.getSize());
        assertEquals("link to SELF", attachmentDTO.getLinks().getLink(CoreLinkKeys.SELF));
    }

    @Test
    void testCreatedByMapper() {
        // given
        CreatedBy createdBy = new CreatedBy("abc-123", "jb@ch.gov.uk", OBJECTOR, "Joe Bloggs", true);

        // when
        CreatedByMapper mapper = Mappers.getMapper(CreatedByMapper.class);
        CreatedByResponseDTO createdByResponseDTO =
                mapper.createdByEntityToCreatedByResponseDTO(createdBy);

        // then
        assertNotNull(createdByResponseDTO);
        assertEquals(OBJECTOR, createdByResponseDTO.getObjector());
        assertEquals("abc-123", createdByResponseDTO.getId());
        assertEquals("jb@ch.gov.uk", createdByResponseDTO.getEmail());
        assertEquals("Joe Bloggs", createdByResponseDTO.getFullName());
        assertTrue(createdByResponseDTO.isShareIdentity());
    }

    @Test
    void willReturnCreatedIfSuccessful()
            throws ServiceException, IOException, ObjectionNotFoundException {
        when(servletRequest.getRequestURI()).thenReturn(ACCESS_URL);
        when(objectionService.addAttachment(
                        anyString(), anyString(), any(MultipartFile.class), anyString()))
                .thenReturn(ServiceResult.accepted("abc"));
        ResponseEntity<ObjectionResponseDTO> entity =
                objectionController.uploadAttachmentToObjection(
                        Utils.mockMultipartFile(), COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID, servletRequest);

        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing POST /{objectionId}/attachments request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Successfully processed POST /{objectionId}/attachments request"),
                        eq(logMap));
    }

    @Test
    void willReturn404IfInvalidRequestSuppliedPostRequest() throws Exception {
        ObjectionNotFoundException objectionNotFoundException =
                new ObjectionNotFoundException("exception error");
        when(servletRequest.getRequestURI()).thenReturn("url");
        when(objectionService.addAttachment(
                        anyString(), anyString(), any(MultipartFile.class), anyString()))
                .thenThrow(objectionNotFoundException);

        ResponseEntity<ObjectionResponseDTO> entity =
                objectionController.uploadAttachmentToObjection(
                        Utils.mockMultipartFile(), COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID, servletRequest);

        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing POST /{objectionId}/attachments request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(eq(REQUEST_ID), contains("Objection not found"), any(), eq(logMap));
    }

    @Test
    void willReturn415FromInvalidUpload()
            throws ServiceException, IOException, ObjectionNotFoundException {
        HttpClientErrorException expectedException =
                new HttpClientErrorException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        when(servletRequest.getRequestURI()).thenReturn(ACCESS_URL);
        when(objectionService.addAttachment(
                        anyString(), anyString(), any(MultipartFile.class), anyString()))
                .thenThrow(expectedException);

        ResponseEntity<ObjectionResponseDTO> entity =
                objectionController.uploadAttachmentToObjection(
                        Utils.mockMultipartFile(), COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID, servletRequest);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, entity.getStatusCode());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing POST /{objectionId}/attachments request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(
                        eq(REQUEST_ID),
                        contains(
                                String.format(
                                        "The file-transfer-api has returned an error for file: %s",
                                        Utils.mockMultipartFile().getOriginalFilename())),
                        any(),
                        eq(logMap));
    }

    @Test
    void willReturn500FromFileTransferServerError()
            throws ServiceException, IOException, ObjectionNotFoundException {
        HttpServerErrorException expectedException =
                new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        when(servletRequest.getRequestURI()).thenReturn("url");
        when(objectionService.addAttachment(
                        anyString(), anyString(), any(MultipartFile.class), anyString()))
                .thenThrow(expectedException);

        ResponseEntity<ObjectionResponseDTO> entity =
                objectionController.uploadAttachmentToObjection(
                        Utils.mockMultipartFile(), COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID, servletRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing POST /{objectionId}/attachments request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(
                        eq(REQUEST_ID),
                        contains(
                                String.format(
                                        "The file-transfer-api has returned an error for file: %s",
                                        Utils.mockMultipartFile().getOriginalFilename())),
                        any(),
                        eq(logMap));
    }

    @Test
    void willReturnInternal500FromFileTransferServerError()
            throws ServiceException, IOException, ObjectionNotFoundException {
        HttpServerErrorException expectedException =
                new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        when(servletRequest.getRequestURI()).thenReturn("url");
        when(objectionService.addAttachment(
                        anyString(), anyString(), any(MultipartFile.class), anyString()))
                .thenThrow(expectedException);

        ResponseEntity<ObjectionResponseDTO> entity =
                objectionController.uploadAttachmentToObjection(
                        Utils.mockMultipartFile(), COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID, servletRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing POST /{objectionId}/attachments request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(
                        eq(REQUEST_ID),
                        contains(
                                String.format(
                                        "The file-transfer-api has returned an error for file: %s",
                                        Utils.mockMultipartFile().getOriginalFilename())),
                        any(),
                        eq(logMap));
    }

    @Test
    void willReturnInternal500FromFileTransferServiceException()
            throws ServiceException, IOException, ObjectionNotFoundException {
        when(servletRequest.getRequestURI()).thenReturn("url");
        when(objectionService.addAttachment(
                        anyString(), anyString(), any(MultipartFile.class), anyString()))
                .thenThrow(new ServiceException("Message"));

        ResponseEntity<ObjectionResponseDTO> entity =
                objectionController.uploadAttachmentToObjection(
                        Utils.mockMultipartFile(), COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID, servletRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing POST /{objectionId}/attachments request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(eq(REQUEST_ID), contains("Objection not found"), any(), eq(logMap));
    }

    @Test
    void getAttachmentTest() throws ObjectionNotFoundException, AttachmentNotFoundException {
        Attachment attachment = new Attachment();
        attachment.setId(ATTACHMENT_ID);
        AttachmentResponseDTO responseDTO = new AttachmentResponseDTO();
        responseDTO.setId(ATTACHMENT_ID);
        when(objectionService.getAttachment(REQUEST_ID, COMPANY_NUMBER, OBJECTION_ID, ATTACHMENT_ID))
                .thenReturn(attachment);
        when(attachmentMapper.attachmentEntityToAttachmentResponseDTO(attachment))
                .thenReturn(responseDTO);
        when(pluggableResponseEntityFactory.createResponse(any()))
                .then(
                        invocation -> {
                            ServiceResult serviceResult = invocation.getArgument(0, ServiceResult.class);
                            return ResponseEntity.status(HttpStatus.OK)
                                    .body(ChResponseBody.createNormalBody(serviceResult.getData()));
                        });

        ResponseEntity<ChResponseBody<AttachmentResponseDTO>> response =
                objectionController.getAttachment(COMPANY_NUMBER, OBJECTION_ID, ATTACHMENT_ID, REQUEST_ID);

        verify(attachmentMapper, times(1)).attachmentEntityToAttachmentResponseDTO(attachment);

        assertEquals(HttpStatus.OK, response.getStatusCode());

        assertNotNull(response.getBody());
        ChResponseBody<AttachmentResponseDTO> responseBody = response.getBody();

        assertNotNull(responseBody.getSuccessBody());
        assertEquals(responseDTO, responseBody.getSuccessBody());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logMap.put(LogConstants.ATTACHMENT_ID.getValue(), ATTACHMENT_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing GET /{objectionId}/attachments/{attachmentId} request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains(
                                "Successfully processed GET /{objectionId}/attachments/{attachmentId} request"),
                        eq(logMap));
    }

    @Test
    void getAttachmentWhenObjectionNotFoundTest()
            throws ObjectionNotFoundException, AttachmentNotFoundException {
        doThrow(new ObjectionNotFoundException("Message"))
                .when(objectionService)
                .getAttachment(any(), any(), any(), any());
        ResponseEntity<ChResponseBody<AttachmentResponseDTO>> response =
                objectionController.getAttachment(COMPANY_NUMBER, OBJECTION_ID, ATTACHMENT_ID, REQUEST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logMap.put(LogConstants.ATTACHMENT_ID.getValue(), ATTACHMENT_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing GET /{objectionId}/attachments/{attachmentId} request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(eq(REQUEST_ID), contains("Objection not found"), any(), eq(logMap));
    }

    @Test
    void getAttachmentWhenAttachmentNotFoundTest()
            throws ObjectionNotFoundException, AttachmentNotFoundException {
        doThrow(new AttachmentNotFoundException("Message"))
                .when(objectionService)
                .getAttachment(any(), any(), any(), any());
        ResponseEntity<ChResponseBody<AttachmentResponseDTO>> response =
                objectionController.getAttachment(COMPANY_NUMBER, OBJECTION_ID, ATTACHMENT_ID, REQUEST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logMap.put(LogConstants.ATTACHMENT_ID.getValue(), ATTACHMENT_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing GET /{objectionId}/attachments/{attachmentId} request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(eq(REQUEST_ID), contains("Attachment not found"), any(), eq(logMap));
    }

    @Test
    void deleteAttachmentTest() {

        ResponseEntity response =
                objectionController.deleteAttachment(
                        COMPANY_NUMBER, OBJECTION_ID, ATTACHMENT_ID, REQUEST_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logMap.put(LogConstants.ATTACHMENT_ID.getValue(), ATTACHMENT_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing DELETE /{objectionId}/attachments/{attachmentId} request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains(
                                "Successfully processed DELETE /{objectionId}/attachments/{attachmentId} request"),
                        eq(logMap));
    }

    @Test
    void deleteAttachmentWhenObjectionNotFoundTest()
            throws ObjectionNotFoundException, AttachmentNotFoundException, ServiceException {
        doThrow(new ObjectionNotFoundException("Message"))
                .when(objectionService)
                .deleteAttachment(any(), any(), any());
        ResponseEntity response =
                objectionController.deleteAttachment(
                        COMPANY_NUMBER, OBJECTION_ID, ATTACHMENT_ID, REQUEST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logMap.put(LogConstants.ATTACHMENT_ID.getValue(), ATTACHMENT_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing DELETE /{objectionId}/attachments/{attachmentId} request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(eq(REQUEST_ID), contains("Objection not found"), any(), eq(logMap));
    }

    @Test
    void deleteAttachmentWhenAttachmentNotFoundTest()
            throws ObjectionNotFoundException, AttachmentNotFoundException, ServiceException {
        doThrow(new AttachmentNotFoundException("Message"))
                .when(objectionService)
                .deleteAttachment(any(), any(), any());
        ResponseEntity response =
                objectionController.deleteAttachment(
                        COMPANY_NUMBER, OBJECTION_ID, ATTACHMENT_ID, REQUEST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logMap.put(LogConstants.ATTACHMENT_ID.getValue(), ATTACHMENT_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing DELETE /{objectionId}/attachments/{attachmentId} request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(eq(REQUEST_ID), contains("Attachment not found"), any(), eq(logMap));
    }

    @Test
    void deleteAttachmentWhenUnableToDelete()
            throws ObjectionNotFoundException, AttachmentNotFoundException, ServiceException {
        doThrow(new ServiceException("Message"))
                .when(objectionService)
                .deleteAttachment(any(), any(), any());
        ResponseEntity response =
                objectionController.deleteAttachment(
                        COMPANY_NUMBER, OBJECTION_ID, ATTACHMENT_ID, REQUEST_ID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logMap.put(LogConstants.ATTACHMENT_ID.getValue(), ATTACHMENT_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("DELETE /{objectionId}/attachments/{attachmentId} request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(eq(REQUEST_ID), contains("Could not delete attachment"), any(), eq(logMap));
    }

    @Test
    void testReturnOkStatusForDownload() {
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        FileTransferApiClientResponse dummyDownloadResponse = Utils.dummyDownloadResponse();
        dummyDownloadResponse.setHttpStatus(HttpStatus.OK);
        when(objectionService.downloadAttachment(
                        REQUEST_ID, OBJECTION_ID, ATTACHMENT_ID, httpServletResponse))
                .thenReturn(dummyDownloadResponse);

        ResponseEntity<Void> responseEntity =
                objectionController.downloadAttachment(
                        COMPANY_NUMBER, OBJECTION_ID, ATTACHMENT_ID, REQUEST_ID, httpServletResponse);

        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        assertTrue(responseEntity.getHeaders().isEmpty());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing GET /{objectionId}/attachments/{attachmentId}/download request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains(
                                "Successfully processed GET /{objectionId}/attachments/{attachmentId}/download"
                                        + " request"),
                        eq(logMap));
    }

    @Test
    void testReturnUnauthorizedStatusForDownload() {
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        FileTransferApiClientResponse dummyDownloadResponse = Utils.dummyDownloadResponse();
        dummyDownloadResponse.setHttpStatus(HttpStatus.UNAUTHORIZED);
        when(objectionService.downloadAttachment(
                        REQUEST_ID, OBJECTION_ID, ATTACHMENT_ID, httpServletResponse))
                .thenReturn(dummyDownloadResponse);

        ResponseEntity<Void> responseEntity =
                objectionController.downloadAttachment(
                        COMPANY_NUMBER, OBJECTION_ID, ATTACHMENT_ID, REQUEST_ID, httpServletResponse);

        assertEquals(HttpStatus.UNAUTHORIZED, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        assertTrue(responseEntity.getHeaders().isEmpty());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing GET /{objectionId}/attachments/{attachmentId}/download request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains(
                                "Successfully processed GET /{objectionId}/attachments/{attachmentId}/download"
                                        + " request"),
                        eq(logMap));
    }

    @Test
    void testReturnForbiddenStatusForDownload() {
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();
        FileTransferApiClientResponse dummyDownloadResponse = Utils.dummyDownloadResponse();
        dummyDownloadResponse.setHttpStatus(HttpStatus.FORBIDDEN);
        when(objectionService.downloadAttachment(
                        REQUEST_ID, OBJECTION_ID, ATTACHMENT_ID, httpServletResponse))
                .thenReturn(dummyDownloadResponse);

        ResponseEntity<Void> responseEntity =
                objectionController.downloadAttachment(
                        COMPANY_NUMBER, OBJECTION_ID, ATTACHMENT_ID, REQUEST_ID, httpServletResponse);

        assertEquals(HttpStatus.FORBIDDEN, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        assertTrue(responseEntity.getHeaders().isEmpty());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing GET /{objectionId}/attachments/{attachmentId}/download request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains(
                                "Successfully processed GET /{objectionId}/attachments/{attachmentId}/download"
                                        + " request"),
                        eq(logMap));
    }

    @Test
    void testDownloadWillCatchHttpClientExceptions() {
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();

        when(objectionService.downloadAttachment(
                        REQUEST_ID, OBJECTION_ID, ATTACHMENT_ID, httpServletResponse))
                .thenThrow(new HttpClientErrorException(HttpStatus.BAD_REQUEST));

        ResponseEntity<Void> responseEntity =
                objectionController.downloadAttachment(
                        COMPANY_NUMBER, OBJECTION_ID, ATTACHMENT_ID, REQUEST_ID, httpServletResponse);
        assertEquals(HttpStatus.BAD_REQUEST, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        assertTrue(responseEntity.getHeaders().isEmpty());
        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing GET /{objectionId}/attachments/{attachmentId}/download request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(eq(REQUEST_ID), contains("Download Error"), any(), eq(logMap));
    }

    @Test
    void testDownloadWillCatchHttpServerExceptions() {
        HttpServletResponse httpServletResponse = new MockHttpServletResponse();

        when(objectionService.downloadAttachment(
                        REQUEST_ID, OBJECTION_ID, ATTACHMENT_ID, httpServletResponse))
                .thenThrow(new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseEntity<Void> responseEntity =
                objectionController.downloadAttachment(
                        COMPANY_NUMBER, OBJECTION_ID, ATTACHMENT_ID, REQUEST_ID, httpServletResponse);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
        assertNull(responseEntity.getBody());
        assertTrue(responseEntity.getHeaders().isEmpty());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), OBJECTION_ID);
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Processing GET /{objectionId}/attachments/{attachmentId}/download request"),
                        eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(eq(REQUEST_ID), contains("Download Error"), any(), eq(logMap));
    }

    @Test
    void isCompanyEligibleForObjectionTestTrueReturned() {
        ObjectionEligibility objectionEligibility =
                new ObjectionEligibility(true, EligibilityStatus.ELIGIBLE);
        when(objectionService.isCompanyEligible(COMPANY_NUMBER, REQUEST_ID))
                .thenReturn(objectionEligibility);
        ResponseEntity<ObjectionEligibility> responseEntity =
                objectionController.isCompanyEligibleForObjection(COMPANY_NUMBER, REQUEST_ID);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertTrue(responseEntity.getBody().isEligible());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logOrder
                .verify(apiLogger)
                .infoContext(eq(REQUEST_ID), contains("Processing GET /eligibility request"), eq(logMap));
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Successfully processed GET /eligibility request"),
                        eq(logMap));
    }

    @Test
    void isCompanyEligibleForObjectionTestFalseReturned() {
        ObjectionEligibility objectionEligibility =
                new ObjectionEligibility(false, EligibilityStatus.INELIGIBLE_COMPANY_STRUCK_OFF);
        when(objectionService.isCompanyEligible(COMPANY_NUMBER, REQUEST_ID))
                .thenReturn(objectionEligibility);
        ResponseEntity<ObjectionEligibility> responseEntity =
                objectionController.isCompanyEligibleForObjection(COMPANY_NUMBER, REQUEST_ID);
        assertEquals(HttpStatus.OK, responseEntity.getStatusCode());
        assertNotNull(responseEntity.getBody());
        assertFalse(responseEntity.getBody().isEligible());

        InOrder logOrder = inOrder(apiLogger);
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);
        logOrder
                .verify(apiLogger)
                .infoContext(eq(REQUEST_ID), contains("Processing GET /eligibility request"), eq(logMap));
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(REQUEST_ID),
                        contains("Successfully processed GET /eligibility request"),
                        eq(logMap));
    }

    @Test
    void testEligibilityEndpointLogsExceptions() {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), COMPANY_NUMBER);

        RuntimeException runtimeException = new RuntimeException();
        when(objectionService.isCompanyEligible(COMPANY_NUMBER, REQUEST_ID))
                .thenThrow(runtimeException);

        ResponseEntity<ObjectionEligibility> responseEntity =
                objectionController.isCompanyEligibleForObjection(COMPANY_NUMBER, REQUEST_ID);

        InOrder logOrder = inOrder(apiLogger);
        logOrder
                .verify(apiLogger)
                .infoContext(eq(REQUEST_ID), contains("Processing GET /eligibility request"), eq(logMap));
        logOrder
                .verify(apiLogger)
                .errorContext(
                        eq(REQUEST_ID), contains("Internal server error"), eq(runtimeException), eq(logMap));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, responseEntity.getStatusCode());
    }
}
