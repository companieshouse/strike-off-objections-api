package uk.gov.companieshouse.api.strikeoffobjections.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.model.company.Gaz2TransactionJson;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.OracleQueryClientException;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ApiSdkClient;

import java.util.Arrays;
import java.util.HashMap;

@Component
public class OracleQueryClient {

    @Autowired
    private ApiLogger apiLogger;

    @Autowired
    ApiSdkClient apiSdkClient;

    @Value("${ORACLE_QUERY_API_URL}")
    private String oracleQueryApiUrl;

    private static final String COMPANY_NUMBER = "company_number";
    private static final String ACTION_CODE_URI_SUFFIX = "/company/%s/action-code";
    private static final String GAZ2_REQUESTED_URI_SUFFIX = "/company/%s/gaz2-requested";
    private static final String ERROR_COMPANY_NUMBER_INVALID = "Company number invalid";
    private static final String ERROR_ACTION_CODE_RETRIEVAL = "Error Retrieving Registered Action Code for Company";
    private static final String ERROR_GAZ2_RETRIEVAL = "Error Retrieving Gaz2 data for Company";


    public Long getCompanyActionCode(String companyNumber, String requestId) {

        var logMap = new HashMap<String, Object>();
        try {
            logMap.put(COMPANY_NUMBER, companyNumber);
            apiLogger.infoContext(requestId, "Retrieving Action Code for Company Number", logMap);
            apiLogger.info("Oracle query API URL: " +  oracleQueryApiUrl);
            apiLogger.debugContext(requestId, "Oracle query API URL: " +  oracleQueryApiUrl);

            var internalApiClient = apiSdkClient.getInternalApiClient();
            internalApiClient.setBasePath(oracleQueryApiUrl);

            return internalApiClient
                    .privateCompanyResourceHandler()
                    .getActionCode(String.format(ACTION_CODE_URI_SUFFIX, companyNumber))
                    .execute()
                    .getData();

        } catch (ApiErrorResponseException e) {
            apiLogger.info(Arrays.toString(e.getStackTrace()));
            apiLogger.info(e.toString());
            apiLogger.errorContext(requestId, ERROR_ACTION_CODE_RETRIEVAL, e, logMap);
            throw new OracleQueryClientException(ERROR_ACTION_CODE_RETRIEVAL);
        } catch (URIValidationException e) {
            apiLogger.errorContext(requestId, ERROR_COMPANY_NUMBER_INVALID, e, logMap);
            throw new OracleQueryClientException(ERROR_COMPANY_NUMBER_INVALID);
        }
    }

    public String getRequestedGaz2(String companyNumber, String requestId) {

        var logMap = new HashMap<String, Object>();
        try {
            logMap.put(COMPANY_NUMBER, companyNumber);
            apiLogger.infoContext(requestId, "Retrieving Gaz2 Data for Company Number", logMap);
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
            apiLogger.errorContext(requestId, ERROR_GAZ2_RETRIEVAL, e, logMap);
            throw new OracleQueryClientException(ERROR_GAZ2_RETRIEVAL);
        } catch (URIValidationException e) {
            apiLogger.errorContext(requestId, ERROR_COMPANY_NUMBER_INVALID, e, logMap);
            throw new OracleQueryClientException(ERROR_COMPANY_NUMBER_INVALID);
        }
    }
}