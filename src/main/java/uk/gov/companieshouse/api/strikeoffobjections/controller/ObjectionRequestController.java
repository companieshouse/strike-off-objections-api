package uk.gov.companieshouse.api.strikeoffobjections.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.LogConstants;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import javax.validation.Valid;

@RestController
@RequestMapping(value = "/company/{companyNumber}/objections/strike-off")
public class ObjectionRequestController {

    private static final String LOG_COMPANY_NUMBER_KEY = LogConstants.COMPANY_NUMBER.getValue();
    private static final String LOG_REQUEST_ID_KEY = LogConstants.REQUEST_ID.getValue();
    private static final String LOG_REASON_KEY = LogConstants.REASON.getValue();
    private static final String ERIC_REQUEST_ID_HEADER = "X-Request-Id";

    private PluggableResponseEntityFactory responseEntityFactory;
    private ApiLogger apiLogger;

    @Autowired
    public ObjectionRequestController(
            PluggableResponseEntityFactory responseEntityFactory,
            ApiLogger apiLogger) {

        this.responseEntityFactory = responseEntityFactory;
        this.apiLogger = apiLogger;
    }

    @PostMapping("/request")
    public ResponseEntity<ChResponseBody<ObjectionResponseBody>> createObjectionRequest(
            @Valid @RequestBody ObjectionRequestBody requestBody,
            @PathVariable("companyNumber") String companyNumber,
            @RequestHeader(value = ERIC_REQUEST_ID_HEADER) String requestId) {
        
        apiLogger.infoContext(
            requestId,
            String.format("POST /request received: %s %s, %s %s, %s %s",
                LOG_REQUEST_ID_KEY, requestId, LOG_COMPANY_NUMBER_KEY, companyNumber, LOG_REASON_KEY, requestBody.getReason()));

        try {
            // TODO Implement actual business logic here when stories are available
            
            ObjectionResponseBody objectionResponseBody = new ObjectionResponseBody();
            objectionResponseBody.setRequestId("12345");
            
            return responseEntityFactory.createResponse(ServiceResult.created(objectionResponseBody));

        } catch (Exception e) {            
            apiLogger.errorContext(
                requestId,
                String.format("Error processing the Strike-Off Objection request for %s %s",
                    LOG_COMPANY_NUMBER_KEY, companyNumber),
                e);

            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        } finally {
            apiLogger.infoContext(
                requestId,
                String.format("Finished POST /current request: %s %s, %s %s",
                    LOG_REQUEST_ID_KEY, requestId,
                    LOG_COMPANY_NUMBER_KEY, companyNumber));
        }
    }
}
