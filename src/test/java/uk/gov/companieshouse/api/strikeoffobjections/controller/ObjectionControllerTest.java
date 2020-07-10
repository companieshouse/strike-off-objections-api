package uk.gov.companieshouse.api.strikeoffobjections.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;
import uk.gov.companieshouse.api.strikeoffobjections.model.response.AttachmentResponseDTO;
import uk.gov.companieshouse.api.strikeoffobjections.model.response.ObjectionResponseDTO;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.links.CoreLinkKeys;
import uk.gov.companieshouse.service.links.Links;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

import static com.mongodb.client.model.Filters.eq;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.ArrayList;
import java.util.List;

@ExtendWith(MockitoExtension.class)
class ObjectionControllerTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String REQUEST_ID = "87654321";
    private static final String OBJECTION_ID = "87651234";
    private static final String REASON = "REASON";
    private static final String ACCESS_URL = "/dummyUrl";

    @Mock
    IObjectionService objectionService;

    @Mock
    ApiLogger apiLogger;

    @Mock
    PluggableResponseEntityFactory pluggableResponseEntityFactory;

    @Mock
    private AttachmentMapper attachmentMapper;

    @Mock
    private HttpServletRequest servletRequest;

    @InjectMocks
    ObjectionController objectionController;

    @Test
    void createObjectionTest() throws Exception {
        ObjectionResponseDTO objectionResponse = new ObjectionResponseDTO(OBJECTION_ID);
        when(objectionService.createObjection(REQUEST_ID, COMPANY_NUMBER)).thenReturn(OBJECTION_ID);
        when(pluggableResponseEntityFactory.createResponse(any(ServiceResult.class))).thenReturn(
                ResponseEntity.status(HttpStatus.CREATED).body(ChResponseBody.createNormalBody(objectionResponse)));
        ResponseEntity<ChResponseBody<ObjectionResponseDTO>> response = objectionController.createObjection(COMPANY_NUMBER, REQUEST_ID);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        assertNotNull(response.getBody());
        ChResponseBody<ObjectionResponseDTO> responseBody = response.getBody();

        assertNotNull(responseBody.getSuccessBody());
        assertEquals(OBJECTION_ID, responseBody.getSuccessBody().getId());
    }

    @Test
    void createObjectionExceptionTest() throws Exception {
        when(objectionService.createObjection(REQUEST_ID, COMPANY_NUMBER)).thenThrow(new Exception("ERROR MESSAGE"));
        when(pluggableResponseEntityFactory.createEmptyInternalServerError()).thenReturn(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        );

        ResponseEntity<ChResponseBody<ObjectionResponseDTO>> response = objectionController.createObjection(COMPANY_NUMBER, REQUEST_ID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    }

    @Test
    void  patchObjectionTest() throws  Exception {
        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(ObjectionStatus.OPEN);
        ResponseEntity response = objectionController.patchObjection(COMPANY_NUMBER, OBJECTION_ID, objectionPatch, REQUEST_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getStatusCode());
    }

    @Test
    void  patchObjectionNotFoundExceptionTest() throws  Exception {
        ObjectionPatch objectionPatch = new ObjectionPatch();
        objectionPatch.setReason(REASON);
        objectionPatch.setStatus(ObjectionStatus.OPEN);
        doThrow(new ObjectionNotFoundException("Message")).when(objectionService).patchObjection(any(), any(), any(), any());
        ResponseEntity response = objectionController.patchObjection(COMPANY_NUMBER, OBJECTION_ID, objectionPatch, REQUEST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void getAttachmentsTest() throws Exception {
        List<Attachment> attachments = new ArrayList<Attachment>();
        Attachment attachment = new Attachment();
        attachments.add(attachment);
        when(objectionService.getAttachments(REQUEST_ID, COMPANY_NUMBER, OBJECTION_ID)).thenReturn(attachments);
        when(pluggableResponseEntityFactory.createResponse(any(ServiceResult.class))).thenReturn(
                ResponseEntity.status(HttpStatus.CREATED).body(ChResponseBody.createNormalBody(attachments)));

        ResponseEntity<ChResponseBody<List<AttachmentResponseDTO>>> response = objectionController.getAttachments(COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID);

        verify(attachmentMapper, times(1)).attachmentEntityToAttachmentResponseDTO(attachment);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        assertNotNull(response.getBody());
        ChResponseBody<List<AttachmentResponseDTO>> responseBody = response.getBody();

        assertNotNull(responseBody.getSuccessBody());
        assertEquals(1, responseBody.getSuccessBody().size());
        assertEquals(attachment, responseBody.getSuccessBody().get(0));
    }

    @Test
    void getAttachmentsObjectionNotFoundExceptionTest() throws Exception {
        doThrow(new ObjectionNotFoundException("Message")).when(objectionService).getAttachments(any(), any(), any());
        ResponseEntity response = objectionController.getAttachments(COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testMapper() {
        //given
        Attachment attachment = new Attachment();
        attachment.setName("xyz");
        attachment.setSize(5);
        Links links = new Links();
        links.setLink(CoreLinkKeys.SELF, "link to SELF");
        attachment.setLinks(links);

        //when
        AttachmentMapper mapper = Mappers.getMapper(AttachmentMapper.class);

        AttachmentResponseDTO attachmentDTO = mapper.attachmentEntityToAttachmentResponseDTO(attachment);

        //then
        assertNotNull(attachmentDTO);
        assertEquals("xyz", attachmentDTO.getName());
        assertEquals(5, attachmentDTO.getSize());
        assertEquals("link to SELF", attachmentDTO.getLinks().getLink(CoreLinkKeys.SELF));
    }

    @Test
    public void willReturnCreatedIfSuccessful() throws ServiceException, IOException, ObjectionNotFoundException {
        when(servletRequest.getRequestURI()).thenReturn(ACCESS_URL);
        when(objectionService.addAttachment(anyString(), anyString(), any(MultipartFile.class), anyString()))
                .thenReturn(ServiceResult.accepted("abc"));
        ResponseEntity entity = objectionController.uploadAttachmentToObjection(Utils.mockMultipartFile(),
                COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID, servletRequest);

        assertEquals(HttpStatus.CREATED, entity.getStatusCode());
    }

    @Test
    public void willReturn404IfInvalidRequestSuppliedPostRequest() throws Exception {
        ObjectionNotFoundException objectionNotFoundException = new ObjectionNotFoundException("exception error");
        when(servletRequest.getRequestURI()).thenReturn("url");
        when(objectionService.addAttachment(anyString(), anyString(), any(MultipartFile.class), anyString()))
                .thenThrow(objectionNotFoundException);

        ResponseEntity entity = objectionController.uploadAttachmentToObjection(Utils.mockMultipartFile(),
                COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID, servletRequest);

        assertEquals(HttpStatus.NOT_FOUND, entity.getStatusCode());
    }

    @Test
    public void willReturn415FromInvalidUpload() throws ServiceException, IOException, ObjectionNotFoundException {
        HttpClientErrorException expectedException =
                new HttpClientErrorException(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
        when(servletRequest.getRequestURI()).thenReturn(ACCESS_URL);
        when(objectionService.addAttachment(anyString(), anyString(), any(MultipartFile.class), anyString()))
                .thenThrow(expectedException);

        ResponseEntity entity = objectionController.uploadAttachmentToObjection(Utils.mockMultipartFile(),
                COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID, servletRequest);

        assertEquals(HttpStatus.UNSUPPORTED_MEDIA_TYPE, entity.getStatusCode());
    }

    @Test
    public void willReturn500FromFileTransferServerError() throws ServiceException, IOException, ObjectionNotFoundException {
        HttpServerErrorException expectedException =
                new HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR);
        when(servletRequest.getRequestURI()).thenReturn("url");
        when(objectionService.addAttachment(anyString(), anyString(), any(MultipartFile.class), anyString()))
                .thenThrow(expectedException);

        ResponseEntity entity = objectionController.uploadAttachmentToObjection(Utils.mockMultipartFile(),
                COMPANY_NUMBER, OBJECTION_ID, REQUEST_ID, servletRequest);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, entity.getStatusCode());
    }
}
