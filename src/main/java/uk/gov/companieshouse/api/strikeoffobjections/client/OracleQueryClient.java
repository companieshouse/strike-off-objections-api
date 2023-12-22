package uk.gov.companieshouse.api.strikeoffobjections.client;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.UnsafeUrlException;

import static uk.gov.companieshouse.api.strikeoffobjections.exception.UnsafeUrlException.ExceptionType.UNSAFE_URL;

@Component
public class OracleQueryClient {

    private final RestTemplate restTemplate;

    private final ApiLogger apiLogger;

    @Value("${ORACLE_QUERY_API_URL}")
    private String oracleQueryApiUrl;

    public OracleQueryClient(RestTemplate restTemplate, ApiLogger apiLogger) {
        this.restTemplate = restTemplate;
        this.apiLogger = apiLogger;
    }

    public Long getCompanyActionCode(String companyNumber, String requestId) {
        String getCompanyActionCodeUrl = formatUrl(companyNumber, "action-code");
        apiLogger.infoContext(requestId, "Calling Oracle Query APi at: " + getCompanyActionCodeUrl);

        // Need this check for cwe, owasp-a5, sans-top25-risky
        if (!getCompanyActionCodeUrl.startsWith(oracleQueryApiUrl)) {
            throw new UnsafeUrlException(UNSAFE_URL, getCompanyActionCodeUrl);
        }

        ResponseEntity<Long> response = restTemplate.getForEntity(getCompanyActionCodeUrl, Long.class);

        return response.getBody();
    }

    public String getRequestedGaz2(String companyNumber, String requestId) {
        String getRequestedGaz2Url = formatUrl(companyNumber, "gaz2-requested");
        apiLogger.infoContext(requestId, "Calling Oracle Query APi at: " + getRequestedGaz2Url);

        // Need this check for cwe, owasp-a5, sans-top25-risky
        if (!getRequestedGaz2Url.startsWith(oracleQueryApiUrl)) {
            throw new UnsafeUrlException(UNSAFE_URL, getRequestedGaz2Url);
        }

        ResponseEntity<String> response = restTemplate.getForEntity(getRequestedGaz2Url, String.class);

        return response.getBody();
    }

    protected String formatUrl(String companyNumber, String action) {
        return String.format("%s/company/%s/%s", oracleQueryApiUrl, companyNumber, action);
    }
}