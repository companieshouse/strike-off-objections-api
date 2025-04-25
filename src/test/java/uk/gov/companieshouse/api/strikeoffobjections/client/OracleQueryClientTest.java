package uk.gov.companieshouse.api.strikeoffobjections.client;

import com.google.api.client.http.HttpHeaders;
import com.google.api.client.http.HttpResponseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.company.PrivateCompanyResourceHandler;
import uk.gov.companieshouse.api.handler.company.request.PrivateCompanyActionCodeGet;
import uk.gov.companieshouse.api.handler.company.request.PrivateCompanyGaz2RequestedGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.Gaz2TransactionJson;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.OracleQueryClientException;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ApiSdkClient;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Unit
@ExtendWith(MockitoExtension.class)
class OracleQueryClientTest {

    private static final String DUMMY_URL = "http://test";
    private static final String COMPANY_NUMBER = "12345678";
    private static final Long ACTION_CODE = 88L;
    private static final String GAZ2_TRANSACTION = "GAZ2";
    private static final String REQUEST_ID = "1234";

    @Mock
    private ApiSdkClient apiSdkClient;

    @Mock
    private PrivateCompanyResourceHandler privateCompanyResourceHandler;

    @Mock
    private PrivateCompanyActionCodeGet privateCompanyActionCodeGet;

    @Mock
    private PrivateCompanyGaz2RequestedGet privateCompanyGaz2RequestedGet;

    @Mock
    private ApiResponse<Long> actionCodeApiResponse;

    @Mock
    private ApiResponse<Gaz2TransactionJson> gaz2ApiResponse;

    @Mock
    private ApiLogger apiLogger;

    @InjectMocks
    private OracleQueryClient oracleQueryClient;

    @Spy
    private OracleQueryClient corruptibleClient;

    @BeforeEach
    public void setup() {
        ReflectionTestUtils.setField(oracleQueryClient, "oracleQueryApiUrl", DUMMY_URL);
        var internalApiClient = mock(InternalApiClient.class);
        when(apiSdkClient.getInternalApiClient()).thenReturn(internalApiClient);
        when(internalApiClient.privateCompanyResourceHandler()).thenReturn(privateCompanyResourceHandler);
    }

    @Test
    void testGetCompanyActionCodeSuccess() throws Exception {
        when(privateCompanyResourceHandler.getActionCode(String.format("/company/%s/action-code", COMPANY_NUMBER)))
                .thenReturn(privateCompanyActionCodeGet);
        when(privateCompanyActionCodeGet.execute()).thenReturn(actionCodeApiResponse);
        when(actionCodeApiResponse.getData()).thenReturn(ACTION_CODE);

        Long actionCode = oracleQueryClient.getCompanyActionCode(COMPANY_NUMBER, REQUEST_ID);

        verify(apiLogger).infoContext(eq(REQUEST_ID), eq("Retrieving Action Code for Company Number "), anyMap());
        verify(apiLogger).debugContext(eq(REQUEST_ID), eq("Oracle query API URL: " + DUMMY_URL));
        assertEquals(ACTION_CODE, actionCode);
    }

    @Test
    void testGetCompanyActionCodeThrowsApiErrorResponseException() throws Exception {
        when(privateCompanyResourceHandler.getActionCode(String.format("/company/%s/action-code", COMPANY_NUMBER)))
                .thenReturn(privateCompanyActionCodeGet);
        when(privateCompanyActionCodeGet.execute()).thenThrow(new ApiErrorResponseException(new HttpResponseException.Builder(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", new HttpHeaders())));

        Exception exception = assertThrows(OracleQueryClientException.class, () -> oracleQueryClient.getCompanyActionCode(COMPANY_NUMBER, REQUEST_ID));
        assertEquals("Error Retrieving Registered Action Code for Company", exception.getMessage());
        verify(apiLogger).errorContext(eq(REQUEST_ID), eq("Error Retrieving Registered Action Code for Company "), any(), anyMap());
    }

    @Test
    void testGetCompanyActionCodeThrowsURIValidationException() throws Exception {
        when(privateCompanyResourceHandler.getActionCode(String.format("/company/%s/action-code", COMPANY_NUMBER)))
                .thenReturn(privateCompanyActionCodeGet);
        when(privateCompanyActionCodeGet.execute()).thenThrow(new URIValidationException("Invalid URI"));

        Exception exception = assertThrows(OracleQueryClientException.class, () -> oracleQueryClient.getCompanyActionCode(COMPANY_NUMBER, REQUEST_ID));
        assertEquals("Company number invalid", exception.getMessage());
        verify(apiLogger).errorContext(eq(REQUEST_ID), eq("Company number invalid"), any(), anyMap());
    }

    @Test
    void testCompanyGaz2RequestedSuccess() throws Exception {
        when(privateCompanyResourceHandler.getGaz2Requested(String.format("/company/%s/gaz2-requested", COMPANY_NUMBER)))
                .thenReturn(privateCompanyGaz2RequestedGet);
        when(privateCompanyGaz2RequestedGet.execute()).thenReturn(gaz2ApiResponse);
        Gaz2TransactionJson gaz2TransactionJson = new Gaz2TransactionJson();
        gaz2TransactionJson.setId(GAZ2_TRANSACTION);
        when(gaz2ApiResponse.getData()).thenReturn(gaz2TransactionJson);

        String gaz2TransactionId = oracleQueryClient.getRequestedGaz2(COMPANY_NUMBER, REQUEST_ID);

        verify(apiLogger).infoContext(eq(REQUEST_ID), eq("Retrieving Gaz2 Data for Company Number "), anyMap());
        verify(apiLogger).debugContext(eq(REQUEST_ID), eq("Oracle query API URL: " + DUMMY_URL));
        assertEquals(GAZ2_TRANSACTION, gaz2TransactionId);
    }

    @Test
    void testCompanyGaz2RequestedSuccessWithNullResponse() throws Exception {
        when(privateCompanyResourceHandler.getGaz2Requested(String.format("/company/%s/gaz2-requested", COMPANY_NUMBER)))
                .thenReturn(privateCompanyGaz2RequestedGet);
        when(privateCompanyGaz2RequestedGet.execute()).thenReturn(gaz2ApiResponse);
        when(gaz2ApiResponse.getData()).thenReturn(null);

        String gaz2TransactionId = oracleQueryClient.getRequestedGaz2(COMPANY_NUMBER, REQUEST_ID);

        verify(apiLogger).infoContext(eq(REQUEST_ID), eq("Retrieving Gaz2 Data for Company Number "), anyMap());
        verify(apiLogger).debugContext(eq(REQUEST_ID), eq("Oracle query API URL: " + DUMMY_URL));
        assertNull(gaz2TransactionId);
    }

    @Test
    void testCompanyGaz2RequestedThrowsApiErrorResponseException() throws Exception {
        when(privateCompanyResourceHandler.getGaz2Requested(String.format("/company/%s/gaz2-requested", COMPANY_NUMBER)))
                .thenReturn(privateCompanyGaz2RequestedGet);
        when(privateCompanyGaz2RequestedGet.execute()).thenThrow(new ApiErrorResponseException(new HttpResponseException.Builder(
                HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal Server Error", new HttpHeaders())));

        Exception exception = assertThrows(OracleQueryClientException.class, () -> oracleQueryClient.getRequestedGaz2(COMPANY_NUMBER, REQUEST_ID));
        assertEquals("Error Retrieving Gaz2 data for Company", exception.getMessage());
        verify(apiLogger).errorContext(eq(REQUEST_ID), eq("Error Retrieving Gaz2 data for Company "), any(), anyMap());
    }

    @Test
    void testCompanyGaz2RequestedThrowsURIValidationException() throws Exception {
        when(privateCompanyResourceHandler.getGaz2Requested(String.format("/company/%s/gaz2-requested", COMPANY_NUMBER)))
                .thenReturn(privateCompanyGaz2RequestedGet);
        when(privateCompanyGaz2RequestedGet.execute()).thenThrow(new URIValidationException("Invalid URI"));

        Exception exception = assertThrows(OracleQueryClientException.class, () -> oracleQueryClient.getRequestedGaz2(COMPANY_NUMBER, REQUEST_ID));
        assertEquals("Company number invalid", exception.getMessage());
        verify(apiLogger).errorContext(eq(REQUEST_ID), eq("Company number invalid"), any(), anyMap());
    }

}