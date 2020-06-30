package uk.gov.companieshouse.api.strikeoffobjections.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.model.response.ObjectionResponse;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.rest.response.ChResponseBody;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObjectionRequestControllerTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String REQUEST_ID = "87654321";
    private static final String OBJECTION_ID = "87651234";

    @Mock
    IObjectionService objectionService;

    @Mock
    ApiLogger apiLogger;

    @Mock
    PluggableResponseEntityFactory pluggableResponseEntityFactory;

    @InjectMocks
    ObjectionRequestController objectionRequestController;

    @Test
    void createObjectionTest() throws Exception{
        ObjectionResponse objectionResponse = new ObjectionResponse(OBJECTION_ID);
        when(objectionService.createObjection(REQUEST_ID, COMPANY_NUMBER)).thenReturn(OBJECTION_ID);
        when(pluggableResponseEntityFactory.createResponse(any(ServiceResult.class))).thenReturn(
                ResponseEntity.status(HttpStatus.CREATED).body(ChResponseBody.createNormalBody(objectionResponse)));
        ResponseEntity<ChResponseBody<ObjectionResponse>> response = objectionRequestController.createObjection(COMPANY_NUMBER, REQUEST_ID);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());

        assertNotNull(response.getBody());
        ChResponseBody<ObjectionResponse> responseBody = response.getBody();

        assertNotNull(responseBody.getSuccessBody());
        assertEquals(OBJECTION_ID, responseBody.getSuccessBody().getId());
    }

    @Test
    void createObjectionExceptionTest() throws Exception{
        when(objectionService.createObjection(REQUEST_ID, COMPANY_NUMBER)).thenThrow(new Exception("ERROR MESSAGE"));
        when(pluggableResponseEntityFactory.createEmptyInternalServerError()).thenReturn(
                ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build()
        );

        ResponseEntity<ChResponseBody<ObjectionResponse>> response = objectionRequestController.createObjection(COMPANY_NUMBER, REQUEST_ID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());

    }
}