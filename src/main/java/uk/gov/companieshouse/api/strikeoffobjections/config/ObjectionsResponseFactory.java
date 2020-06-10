package uk.gov.companieshouse.api.strikeoffobjections.config;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.ServiceResultStatus;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.ResponseEntityFactory;


public class ObjectionsResponseFactory implements ResponseEntityFactory {
        
    @Override
    public <T> ResponseEntity<ChResponseBody<T>> createResponseEntity(
            ServiceResult<T> serviceResult) {
        ChResponseBody<T> body = ChResponseBody.createNormalBody(serviceResult.getData());
        
        return ResponseEntity.status(HttpStatus.CREATED).body(body); 
    }

    @Override
    public ServiceResultStatus getStatusToMatch() {
        return ServiceResultStatus.CREATED;
    }
}

