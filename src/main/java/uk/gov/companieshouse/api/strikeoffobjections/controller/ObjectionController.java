package uk.gov.companieshouse.api.strikeoffobjections.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClient;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;
import uk.gov.companieshouse.api.strikeoffobjections.model.response.ObjectionResponse;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/company/{companyNumber}/strike-off-objections")
public class ObjectionController {

    private static final String LOG_COMPANY_NUMBER_KEY = LogConstants.COMPANY_NUMBER.getValue();
    private static final String LOG_OBJECTION_ID_KEY = LogConstants.OBJECTION_ID.getValue();
    private static final String ERIC_REQUEST_ID_HEADER = "X-Request-Id";

    private PluggableResponseEntityFactory responseEntityFactory;
    private IObjectionService objectionService;

    private ApiLogger apiLogger;

    @Autowired
    public ObjectionController(PluggableResponseEntityFactory responseEntityFactory,
                               IObjectionService objectionService,
                               ApiLogger apiLogger) {
        this.responseEntityFactory = responseEntityFactory;
        this.objectionService = objectionService;
        this.apiLogger = apiLogger;
    }

    @PostMapping
    public ResponseEntity<ChResponseBody<ObjectionResponse>> createObjection(
            @PathVariable("companyNumber") String companyNumber,
            @RequestHeader(value = ERIC_REQUEST_ID_HEADER) String requestId
    ) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LOG_COMPANY_NUMBER_KEY, companyNumber);

        apiLogger.infoContext(
                requestId,
                "POST / request received",
                logMap
        );

        try {
            String objectionId = objectionService.createObjection(requestId, companyNumber);
            ObjectionResponse response = new ObjectionResponse(objectionId);
            return responseEntityFactory.createResponse(ServiceResult.created(response));
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

    @PatchMapping("/{objectionId}")
    public ResponseEntity patchObjection(
            @PathVariable("companyNumber") String companyNumber,
            @PathVariable("objectionId") String objectionId,
            @RequestBody ObjectionPatch objectionPatch,
            @RequestHeader(value = ERIC_REQUEST_ID_HEADER) String requestId
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
            objectionService.patchObjection(requestId, companyNumber, objectionId, objectionPatch);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ObjectionNotFoundException e) {

            apiLogger.errorContext(
                    requestId,
                    "Objection not found",
                    e,
                    logMap
            );

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } finally {
            apiLogger.infoContext(
                    requestId,
                    "Finished PATCH /{objectionId} request",
                    logMap
            );
        }
    }

    @PostMapping("/{objectionId}/attachments")
    public ResponseEntity<String> uploadAttachmentToObjection (
            @RequestParam("file") MultipartFile file,
            @PathVariable("companyNumber") String companyNumber,
            @PathVariable String objectionId,
            @RequestHeader(value = ERIC_REQUEST_ID_HEADER) String requestId) {

        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LOG_COMPANY_NUMBER_KEY, companyNumber);
        logMap.put(LOG_OBJECTION_ID_KEY, objectionId);

        apiLogger.infoContext(
                requestId,
                "POST /{objectionId}/attachments request received",
                logMap
        );

        try {
            ServiceResult<String> result = objectionService.addAttachment(objectionId, file);
            return new ResponseEntity(result.getData(), HttpStatus.OK);
        } catch(ServiceException e) {

            apiLogger.errorContext(
                    requestId,
                    "Objection not found",
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
        } finally {
            apiLogger.infoContext(
                     requestId,
                    "Finished POST /{objectionId}/attachments request",
                    logMap
            );
        }
    }
}
