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
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.LogConstants;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.model.request.ObjectionRequest;
import uk.gov.companieshouse.api.strikeoffobjections.model.response.ObjectionResponse;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping(value = "/company/{companyNumber}/strike-off-objections")
public class ObjectionRequestController {

    private static final String LOG_COMPANY_NUMBER_KEY = LogConstants.COMPANY_NUMBER.getValue();
    private static final String LOG_OBJECTION_ID_KEY = LogConstants.OBJECTION_ID.getValue();
    private static final String ERIC_REQUEST_ID_HEADER = "X-Request-Id";

    private PluggableResponseEntityFactory responseEntityFactory;
    private IObjectionService objectionService;
    private ApiLogger apiLogger;

    @Autowired
    public ObjectionRequestController(PluggableResponseEntityFactory responseEntityFactory, IObjectionService objectionService, ApiLogger apiLogger) {
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
    public ResponseEntity<ChResponseBody<ObjectionResponse>> patchObjection(
            @PathVariable("companyNumber") String companyNumber,
            @PathVariable("objectionId") String objectionId,
            @RequestBody ObjectionRequest objectionRequest,
            @RequestHeader(value = ERIC_REQUEST_ID_HEADER) String requestId
    ) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LOG_COMPANY_NUMBER_KEY, companyNumber);

        apiLogger.infoContext(
                requestId,
                "PATCH /{objectionId} request received",
                logMap
        );

        try {
            objectionService.patchObjection(requestId, companyNumber, objectionId, objectionRequest);

            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (ObjectionNotFoundException e) {
            logMap.put(LOG_OBJECTION_ID_KEY, objectionId);

            apiLogger.errorContext(
                    requestId,
                    "Objection not found",
                    e,
                    logMap
            );

            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            logMap.put(LOG_OBJECTION_ID_KEY, objectionId);

            apiLogger.errorContext(
                    requestId,
                    "Error patching the Strike-Off Objection",
                    e,
                    logMap
            );

            return responseEntityFactory.createEmptyInternalServerError();
        } finally {
            apiLogger.infoContext(
                    requestId,
                    "Finished PATCH /{objectionId} request",
                    logMap
            );
        }
    }
}
