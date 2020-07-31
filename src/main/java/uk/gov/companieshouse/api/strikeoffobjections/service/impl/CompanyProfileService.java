package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.ApiClient;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.LogConstants;
import uk.gov.companieshouse.api.strikeoffobjections.service.ICompanyProfileService;
import uk.gov.companieshouse.service.ServiceException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
public class CompanyProfileService implements ICompanyProfileService {
    private static final String LOG_COMPANY_NUMBER_KEY = LogConstants.COMPANY_NUMBER.getValue();

    private ApiSdkClient apiSdkClient;
    private ApiLogger apiLogger;

    @Autowired
    public CompanyProfileService(ApiSdkClient apiSdkClient,
                                 ApiLogger apiLogger) {
        this.apiSdkClient = apiSdkClient;
        this.apiLogger = apiLogger;
    }

    /**
     * Calls the API SDK Manager Java library in order to retrieve the company (profile) details.
     */
    public CompanyProfileApi getCompanyProfile(String companyNumber, String requestId) throws ServiceException {
        try {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LOG_COMPANY_NUMBER_KEY, companyNumber);

            ApiClient apiClient = apiSdkClient.getApiClient();

            String companyProfileUrl = String.format("/company/%s", companyNumber);

            apiLogger.infoContext(
                    requestId,
                    "Retrieving company details from the SDK",
                    logMap
            );

            return apiClient.company().get(companyProfileUrl).execute().getData();
        } catch (IOException | URIValidationException e) {
            apiLogger.errorContext(
                    requestId,
                    e);

            throw new ServiceException(
                    String.format("Problem retrieving company details from the SDK for %s %s",
                            LOG_COMPANY_NUMBER_KEY, companyNumber),
                    e);
        }
    }
}
