package uk.gov.companieshouse.api.strikeoffobjections.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.company.ActionCodeJson;
import uk.gov.companieshouse.api.model.company.Gaz2TransactionJson;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.OracleQueryClientException;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ApiSdkClient;
import java.util.HashMap;

@Component
public class OracleQueryClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiLogger apiLogger;

    @Autowired
    ApiSdkClient apiSdkClient;

    @Value("${ORACLE_QUERY_API_URL}")
    private String oracleQueryApiUrl;

    private static final String COMPANY_NUMBER = "company_number";
    private static final String ACTION_CODE_URI_SUFFIX = "/company/%s/action-code";
    private static final String GAZ2_REQUESTED_URI_SUFFIX = "/company/%s/gaz2-requested";


    public Long getCompanyActionCode(String companyNumber, String requestId) {

        var logMap = new HashMap<String, Object>();
        try {
            logMap.put(COMPANY_NUMBER, companyNumber);
            apiLogger.infoContext(requestId, "Retrieving Action Code for Company Number ", logMap);
            apiLogger.debugContext(requestId, "Oracle query API URL: " +  oracleQueryApiUrl);

            var internalApiClient = apiSdkClient.getInternalApiClient();
            internalApiClient.setBasePath(oracleQueryApiUrl);
            ActionCodeJson apiResponse = internalApiClient
                    .privateCompanyResourceHandler()
                    .getActionCode(String.format(ACTION_CODE_URI_SUFFIX, companyNumber))
                    .execute()
                    .getData();

            return Long.parseLong(apiResponse.getActionCode());

        } catch (ApiErrorResponseException e) {
            apiLogger.errorContext(requestId, "Error Retrieving Registered Action Code for Company ", e, logMap);
            throw new OracleQueryClientException("Error Retrieving Registered Action Code for Company");
        } catch (URIValidationException e) {
            apiLogger.errorContext(requestId, "Company number invalid", e, logMap);
            throw new OracleQueryClientException("Company number invalid");
        } catch (NumberFormatException e) {
            apiLogger.errorContext(requestId, "Action Code could not be parsed from the response json", e, logMap);
            throw new OracleQueryClientException("Action Code could not be parsed from the response json");
        }
    }

    public String getRequestedGaz2(String companyNumber, String requestId) {

        var logMap = new HashMap<String, Object>();
        try {
            logMap.put(COMPANY_NUMBER, companyNumber);
            apiLogger.infoContext(requestId, "Retrieving Gaz2 Data for Company Number ", logMap);
            apiLogger.debugContext(requestId, "Oracle query API URL: " +  oracleQueryApiUrl);

            var internalApiClient = apiSdkClient.getInternalApiClient();
            internalApiClient.setBasePath(oracleQueryApiUrl);
            Gaz2TransactionJson apiResponse = internalApiClient
                    .privateCompanyResourceHandler()
                    .getGaz2Requested(String.format(GAZ2_REQUESTED_URI_SUFFIX, companyNumber))
                    .execute()
                    .getData();

            if (apiResponse == null) {
                return null;
            }

            return apiResponse.getId();

        } catch (ApiErrorResponseException e) {
            apiLogger.errorContext(requestId, "Error Retrieving Gaz2 data for Company ", e, logMap);
            throw new OracleQueryClientException("Error Retrieving Gaz2 data for Company");
        } catch (URIValidationException e) {
            apiLogger.errorContext(requestId, "Company number invalid", e, logMap);
            throw new OracleQueryClientException("Company number invalid");
        }
    }

    protected String formatUrl(String companyNumber, String action) {
        return String.format("%s/company/%s/%s", oracleQueryApiUrl, companyNumber, action);
    }
}