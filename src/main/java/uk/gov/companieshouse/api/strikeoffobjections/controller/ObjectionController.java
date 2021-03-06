package uk.gov.companieshouse.api.strikeoffobjections.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.LogConstants;
import uk.gov.companieshouse.api.strikeoffobjections.exception.AttachmentNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.api.strikeoffobjections.model.create.ObjectionCreate;
import uk.gov.companieshouse.api.strikeoffobjections.model.eligibility.ObjectionEligibility;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;
import uk.gov.companieshouse.api.strikeoffobjections.model.response.AttachmentResponseDTO;
import uk.gov.companieshouse.api.strikeoffobjections.model.response.ObjectionResponseDTO;
import uk.gov.companieshouse.api.strikeoffobjections.exception.InvalidObjectionStatusException;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderFields.ERIC_REQUEST_ID;
import static uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderFields.ERIC_IDENTITY;
import static uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderFields.ERIC_AUTHORISED_USER;

@RestController
@RequestMapping(value = "/company/{companyNumber}/strike-off-objections")
public class ObjectionController {

    private static final String LOG_COMPANY_NUMBER_KEY = LogConstants.COMPANY_NUMBER.getValue();
    private static final String LOG_OBJECTION_ID_KEY = LogConstants.OBJECTION_ID.getValue();
    private static final String LOG_ATTACHMENT_ID = LogConstants.ATTACHMENT_ID.getValue();
    private static final String OBJECTION_NOT_FOUND = "Objection not found";
    private static final String ATTACHMENT_NOT_FOUND = "Attachment not found";
    private static final String ERROR_500 = "Internal server error";
    private static final String COULD_NOT_DELETE = "Could not delete attachment";
    private static final String OBJECTION_NOT_PROCESSED = "Objection not processed";
    private static final String DOWNLOAD_ERROR = "Download Error";

    private PluggableResponseEntityFactory responseEntityFactory;
    private IObjectionService objectionService;

    private ApiLogger apiLogger;
    private ObjectionMapper objectionMapper;
    private AttachmentMapper attachmentMapper;

    @Autowired
    public ObjectionController(PluggableResponseEntityFactory responseEntityFactory,
                               IObjectionService objectionService,
                               ApiLogger apiLogger,
                               ObjectionMapper objectionMapper,
                               AttachmentMapper attachmentMapper) {
        this.responseEntityFactory = responseEntityFactory;
        this.objectionService = objectionService;
        this.apiLogger = apiLogger;
        this.objectionMapper = objectionMapper;
        this.attachmentMapper = attachmentMapper;
    }

    @GetMapping("/{objectionId}/attachments/{attachmentId}")
    public ResponseEntity<ChResponseBody<AttachmentResponseDTO>> getAttachment(
            @PathVariable("companyNumber") String companyNumber,
            @PathVariable("objectionId") String objectionId,
            @PathVariable("attachmentId") String attachmentId,
            @RequestHeader(value = ERIC_REQUEST_ID) String requestId
    ) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LOG_COMPANY_NUMBER_KEY, companyNumber);
        logMap.put(LOG_OBJECTION_ID_KEY, objectionId);
        logMap.put(LOG_ATTACHMENT_ID, attachmentId);

        apiLogger.infoContext(
                requestId,
                "GET /{objectionId}/attachments/{attachmentId} request received",
                logMap
        );

        try {
            Attachment attachment = objectionService.getAttachment(requestId, companyNumber, objectionId, attachmentId);
            AttachmentResponseDTO responseDTO = attachmentMapper.attachmentEntityToAttachmentResponseDTO(attachment);

            return responseEntityFactory.createResponse(ServiceResult.found(responseDTO));
        } catch (ObjectionNotFoundException e) {
            apiLogger.errorContext(
                    requestId,
                    OBJECTION_NOT_FOUND,
                    e,
                    logMap
            );

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AttachmentNotFoundException e) {
            apiLogger.errorContext(
                    requestId,
                    ATTACHMENT_NOT_FOUND,
                    e,
                    logMap
            );

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } finally {
            apiLogger.infoContext(
                    requestId,
                    "Finished GET /{objectionId}/attachments/{attachmentId} request",
                    logMap
            );
        }
    }

    @PostMapping
    public ResponseEntity<ChResponseBody<ObjectionResponseDTO>> createObjection(
            @PathVariable("companyNumber") String companyNumber,
            @RequestHeader(value = ERIC_REQUEST_ID) String requestId,
            @RequestHeader(value = ERIC_IDENTITY) String ericUserId,
            @RequestHeader(value = ERIC_AUTHORISED_USER) String ericUserDetails,
            @RequestBody ObjectionCreate objectionCreate
    ) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LOG_COMPANY_NUMBER_KEY, companyNumber);

        apiLogger.infoContext(
                requestId,
                "POST / request received",
                logMap
        );

        try {
            Objection objection = objectionService.createObjection(
                    requestId,
                    companyNumber,
                    ericUserId,
                    ericUserDetails,
                    objectionCreate);
            ObjectionStatus objectionStatus = objection.getStatus();
            ObjectionResponseDTO responseDTO = new ObjectionResponseDTO(objection.getId());
            responseDTO.setStatus(objectionStatus);
            return responseEntityFactory.createResponse(ServiceResult.created(responseDTO));
        } catch (Exception e) {
            apiLogger.errorContext(
                    requestId,
                    "Error creating the Strike-Off Objection",
                    e,
                    logMap
            );

            return responseEntityFactory.createEmptyInternalServerError();
        } finally {
            apiLogger.infoContext(
                    requestId,
                    "Finished POST / request",
                    logMap
            );
        }
    }

    /**
     * Updates Objection data and will process the objection if status
     * updated from OPEN to SUBMITTED
     *
     * @param companyNumber  the company number
     * @param objectionId    id of objection record in database
     * @param objectionPatch data to apply to the objection
     * @param requestId      http request id used for logging
     * @return ResponseEntity the api response
     */
    @PatchMapping("/{objectionId}")
    public ResponseEntity<Void> patchObjection(
            @PathVariable("companyNumber") String companyNumber,
            @PathVariable("objectionId") String objectionId,
            @RequestBody ObjectionPatch objectionPatch,
            @RequestHeader(value = ERIC_REQUEST_ID) String requestId
    ) {

        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LOG_COMPANY_NUMBER_KEY, companyNumber);
        logMap.put(LOG_OBJECTION_ID_KEY, objectionId);

        apiLogger.infoContext(
                requestId,
                "PATCH /{objectionId} request received",
                logMap
        );

        try {
            objectionService.patchObjection(objectionId, objectionPatch, requestId, companyNumber);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ObjectionNotFoundException e) {
            apiLogger.errorContext(
                    requestId,
                    OBJECTION_NOT_FOUND,
                    e,
                    logMap
            );

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        } catch (InvalidObjectionStatusException iose) {
            apiLogger.errorContext(
                    requestId,
                    OBJECTION_NOT_PROCESSED,
                    iose,
                    logMap
            );

            return new ResponseEntity<>(HttpStatus.UNPROCESSABLE_ENTITY);

        } catch (Exception e) {
            apiLogger.errorContext(
                    requestId,
                    e.getMessage(),
                    e,
                    logMap
            );

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        } finally {
            apiLogger.infoContext(
                    requestId,
                    "Finished PATCH /{objectionId} request",
                    logMap
            );
        }
    }

    @GetMapping("/{objectionId}")
    public ResponseEntity<ChResponseBody<ObjectionResponseDTO>> getObjection(
            @PathVariable("companyNumber") String companyNumber,
            @PathVariable("objectionId") String objectionId,
            @RequestHeader(value = ERIC_REQUEST_ID) String requestId
    ) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LOG_COMPANY_NUMBER_KEY, companyNumber);
        logMap.put(LOG_OBJECTION_ID_KEY, objectionId);

        apiLogger.infoContext(
                requestId,
                "GET /{objectionId} request received",
                logMap
        );

        try {
            Objection objection = objectionService.getObjection(requestId, objectionId);
            ObjectionResponseDTO responseDTO =
                    objectionMapper.objectionEntityToObjectionResponseDTO(objection);
            return responseEntityFactory.createResponse(ServiceResult.found(responseDTO));
        } catch (ObjectionNotFoundException e) {
            apiLogger.errorContext(
                    requestId,
                    OBJECTION_NOT_FOUND,
                    e,
                    logMap
            );

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            apiLogger.errorContext(
                    requestId,
                    ERROR_500,
                    e,
                    logMap
            );

            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        } finally {
            apiLogger.infoContext(
                    requestId,
                    "Finished GET /{objectionId} request",
                    logMap
            );
        }
    }

    @GetMapping("/{objectionId}/attachments")
    public ResponseEntity<ChResponseBody<List<AttachmentResponseDTO>>> getAttachments(
            @PathVariable("companyNumber") String companyNumber,
            @PathVariable("objectionId") String objectionId,
            @RequestHeader(value = ERIC_REQUEST_ID) String requestId
    ) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LOG_COMPANY_NUMBER_KEY, companyNumber);
        logMap.put(LOG_OBJECTION_ID_KEY, objectionId);

        apiLogger.infoContext(
                requestId,
                "GET /{objectionId}/attachments request received",
                logMap
        );

        try {
            List<Attachment> attachments = objectionService.getAttachments(requestId, companyNumber, objectionId);

            List<AttachmentResponseDTO> attachmentResponseDTOs = attachments.stream()
                    .map(attachmentMapper::attachmentEntityToAttachmentResponseDTO).collect(Collectors.toList());

            return responseEntityFactory.createResponse(ServiceResult.found(attachmentResponseDTOs));
        } catch (ObjectionNotFoundException e) {
            apiLogger.errorContext(
                    requestId,
                    OBJECTION_NOT_FOUND,
                    e,
                    logMap
            );

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } finally {
            apiLogger.infoContext(
                    requestId,
                    "Finished GET /{objectionId}/attachments request",
                    logMap
            );
        }
    }

    @PostMapping("/{objectionId}/attachments")
    public ResponseEntity<ObjectionResponseDTO> uploadAttachmentToObjection (
            @RequestParam("file") MultipartFile file,
            @PathVariable("companyNumber") String companyNumber,
            @PathVariable String objectionId,
            @RequestHeader(value = ERIC_REQUEST_ID) String requestId,
            HttpServletRequest servletRequest) {

        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LOG_COMPANY_NUMBER_KEY, companyNumber);
        logMap.put(LOG_OBJECTION_ID_KEY, objectionId);

        apiLogger.infoContext(
                requestId,
                "POST /{objectionId}/attachments request received",
                logMap
        );

        try {
            ServiceResult<String> result = objectionService.addAttachment(requestId, objectionId, file, servletRequest.getRequestURI());
            ObjectionResponseDTO objectionResponseDTO = new ObjectionResponseDTO(result.getData());
            return new ResponseEntity<>(objectionResponseDTO, HttpStatus.CREATED);
        } catch(ServiceException e) {

            apiLogger.errorContext(
                    requestId,
                    OBJECTION_NOT_FOUND,
                    e,
                    logMap
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } catch(HttpClientErrorException | HttpServerErrorException e) {

            apiLogger.errorContext(
                    requestId,
                    String.format("The file-transfer-api has returned an error for file: %s",
                            file.getOriginalFilename()),
                    e,
                    logMap
            );
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (ObjectionNotFoundException e) {
            apiLogger.errorContext(
                    requestId,
                    OBJECTION_NOT_FOUND,
                    e,
                    logMap
            );

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } finally {
            apiLogger.infoContext(
                    requestId,
                    "Finished POST /{objectionId}/attachments request",
                    logMap
            );
        }
    }

    @DeleteMapping("/{objectionId}/attachments/{attachmentId}")
    public ResponseEntity<Void> deleteAttachment(
            @PathVariable String companyNumber,
            @PathVariable String objectionId,
            @PathVariable String attachmentId,
            @RequestHeader(value = ERIC_REQUEST_ID) String requestId
    ) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LOG_COMPANY_NUMBER_KEY, companyNumber);
        logMap.put(LOG_OBJECTION_ID_KEY, objectionId);
        logMap.put(LOG_ATTACHMENT_ID, attachmentId);

        apiLogger.infoContext(
                requestId,
                "DELETE /{objectionId}/attachments/{attachmentId} request received",
                logMap
        );

        try {
            objectionService.deleteAttachment(requestId, objectionId, attachmentId);

            return ResponseEntity.noContent().build();
        } catch (ObjectionNotFoundException e) {
            apiLogger.errorContext(
                    requestId,
                    OBJECTION_NOT_FOUND,
                    e,
                    logMap
            );

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (AttachmentNotFoundException e) {
            apiLogger.errorContext(
                    requestId,
                    ATTACHMENT_NOT_FOUND,
                    e,
                    logMap
            );

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        } catch (ServiceException e) {
            apiLogger.errorContext(
                    requestId,
                    COULD_NOT_DELETE,
                    e,
                    logMap
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } finally {
            apiLogger.infoContext(
                    requestId,
                    "Finished DELETE /{objectionId}/attachments/{attachmentId} request",
                    logMap
            );
        }
    }

    @GetMapping("/{objectionId}/attachments/{attachmentId}/download")
    public ResponseEntity<Void> downloadAttachment(@PathVariable String companyNumber,
                                                   @PathVariable String objectionId,
                                                   @PathVariable String attachmentId,
                                                   @RequestHeader(value = ERIC_REQUEST_ID) String requestId,
                                                   HttpServletResponse response) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LOG_COMPANY_NUMBER_KEY, companyNumber);
        logMap.put(LOG_OBJECTION_ID_KEY, objectionId);

        try {
            apiLogger.infoContext(
                    requestId,
                    "GET /{objectionId}/attachments/{attachmentId}/download request received",
                    logMap
            );
            FileTransferApiClientResponse downloadServiceResult = objectionService.downloadAttachment(
                    requestId, objectionId, attachmentId, response);
            return ResponseEntity.status(downloadServiceResult.getHttpStatus()).build();
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            apiLogger.errorContext(
                    requestId,
                    DOWNLOAD_ERROR,
                    e,
                    logMap
            );
            return ResponseEntity.status(e.getStatusCode()).build();
        } catch (ServiceException e) {
            apiLogger.errorContext(
                    requestId,
                    DOWNLOAD_ERROR,
                    e,
                    logMap
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();

        } finally {
            apiLogger.infoContext(
                    requestId,
                    "Finished GET /{objectionId}/attachments/{attachmentId}/download request",
                    logMap
            );
        }
    }

    @GetMapping("/eligibility")
    public ResponseEntity<ObjectionEligibility> isCompanyEligibleForObjection(@PathVariable("companyNumber") String companyNumber,
                                                                              @RequestHeader(value = ERIC_REQUEST_ID) String requestId) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LOG_COMPANY_NUMBER_KEY, companyNumber);

        apiLogger.infoContext(
                requestId,
                "GET /eligibility request received",
                logMap);

        try {
            ObjectionEligibility result = objectionService.isCompanyEligible(companyNumber, requestId);
            return new ResponseEntity<>(result, HttpStatus.OK);

        } catch (Exception e) {
            apiLogger.errorContext(
                    requestId,
                    ERROR_500,
                    e,
                    logMap
            );
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);

        } finally {
            apiLogger.infoContext(
                    requestId,
                    "Finished GET /eligibility request",
                    logMap);
        }
    }
}
