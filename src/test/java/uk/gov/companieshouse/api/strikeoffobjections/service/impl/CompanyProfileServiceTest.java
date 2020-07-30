package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.company.CompanyResourceHandler;
import uk.gov.companieshouse.api.handler.company.request.CompanyGet;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.service.ServiceException;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CompanyProfileServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String REQUEST_ID = "87654321";

    @Mock
    private ApiLogger apiLogger;
    @Mock
    private ApiSdkClient apiSdkClient;

    @Mock
    private ApiClient apiClient;

    @Mock
    private CompanyResourceHandler companyResourceHandler;

    @Mock
    private CompanyGet companyGet;

    @Mock
    private ApiResponse<CompanyProfileApi> apiResponse;

    @InjectMocks
    private CompanyProfileService companyProfileService;

    private CompanyProfileApi dummyCompanyProfile;

    @BeforeEach
    void init() {
        dummyCompanyProfile = Utils.getDummyCompanyProfile(COMPANY_NUMBER);
    }

    @Test
    void testGetCompanyProfile() throws ApiErrorResponseException, URIValidationException, ServiceException {
        when(apiSdkClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.company()).thenReturn(companyResourceHandler);
        when(companyResourceHandler.get("/company/" + COMPANY_NUMBER)).thenReturn(companyGet);
        when(companyGet.execute()).thenReturn(apiResponse);
        when(apiResponse.getData()).thenReturn(dummyCompanyProfile);

        CompanyProfileApi returnedCompanyProfile = companyProfileService.getCompanyProfile(COMPANY_NUMBER, REQUEST_ID);

        assertEquals(dummyCompanyProfile, returnedCompanyProfile);
    }

    @Test
    void testGetCompanyProfileThrowsServiceExceptionIfCallFails()
            throws ApiErrorResponseException, URIValidationException {
        when(apiSdkClient.getApiClient()).thenReturn(apiClient);
        when(apiClient.company()).thenReturn(companyResourceHandler);
        when(companyResourceHandler.get("/company/" + COMPANY_NUMBER)).thenReturn(companyGet);
        when(companyGet.execute()).thenThrow(ApiErrorResponseException.fromIOException(new IOException("Error")));

        assertThrows(
                ServiceException.class,
                () -> companyProfileService.getCompanyProfile(COMPANY_NUMBER, REQUEST_ID)
        );
    }
}
