package uk.gov.companieshouse.api.strikeoffobjections.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import tools.jackson.databind.exc.MismatchedInputException;
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
    private static final String ERROR_GAZ2_MISMATCHED_INPUT = "Mismatched input error occurred while retrieving Gaz2 data for Company";
    private static final String ERROR_OTHER = "Unexpected error occurred while retrieving Gaz2 data for Company";
    private static final String EMPTY_BODY_MISMATCH_MESSAGE = "No content to map due to end-of-input";


    public Long getCompanyActionCode(String companyNumber, String requestId) {

        var logMap = new HashMap<String, Object>();
        try {
            logMap.put(COMPANY_NUMBER, companyNumber);
            apiLogger.infoContext(requestId, "Retrieving Action Code for Company Number", logMap);
            apiLogger.info("Request id: " + requestId + ", Oracle URL " +  oracleQueryApiUrl);
            apiLogger.debugContext(requestId, "Oracle query API URL: " +  oracleQueryApiUrl);

            var internalApiClient = apiSdkClient.getInternalApiClient();

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
            apiLogger.info(Arrays.toString(e.getStackTrace()));
            apiLogger.info(e.toString());
            apiLogger.errorContext(requestId, ERROR_GAZ2_RETRIEVAL, e, logMap);
            throw new OracleQueryClientException(ERROR_GAZ2_RETRIEVAL);
        } catch (URIValidationException e) {
            apiLogger.errorContext(requestId, ERROR_COMPANY_NUMBER_INVALID, e, logMap);
            throw new OracleQueryClientException(ERROR_COMPANY_NUMBER_INVALID);
        } catch (MismatchedInputException e) {
            if (isEmptyResponseBody(e)) {
                apiLogger.infoContext(requestId, "No Gaz2 data found for company (empty response body), returning null", logMap);
                return null;
            }
            apiLogger.errorContext(requestId, ERROR_GAZ2_MISMATCHED_INPUT, e, logMap);
            throw new OracleQueryClientException(ERROR_GAZ2_MISMATCHED_INPUT);
        } catch (Exception e) {
            apiLogger.errorContext(requestId, ERROR_OTHER, e, logMap);
            throw new OracleQueryClientException(ERROR_OTHER);
        }
    }

    private boolean isEmptyResponseBody(MismatchedInputException e) {
        return e.getMessage() != null && e.getMessage().contains(EMPTY_BODY_MISMATCH_MESSAGE);
    }
}