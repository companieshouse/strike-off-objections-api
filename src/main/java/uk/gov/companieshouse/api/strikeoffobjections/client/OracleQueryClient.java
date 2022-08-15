package uk.gov.companieshouse.api.strikeoffobjections.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;

@Component
public class OracleQueryClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiLogger apiLogger;

    @Value("${ORACLE_QUERY_API_URL}")
    private String oracleQueryApiUrl;

    public Long getCompanyActionCode(String companyNumber, String requestId) {
        String getCompanyActionCodeUrl = String.format("%s/company/%s/action-code", oracleQueryApiUrl, companyNumber);
        apiLogger.infoContext(requestId, "Calling Oracle Query APi at: " + getCompanyActionCodeUrl);

        ResponseEntity<Long> response = restTemplate.getForEntity(getCompanyActionCodeUrl, Long.class);

        return response.getBody();
    }

    public String getRequestedGaz2(String companyNumber, String requestId) {
        String getRequestedGaz2Url = String.format("%s/company/%s/gaz2-requested", oracleQueryApiUrl, companyNumber);
        apiLogger.infoContext(requestId, "Calling Oracle Query APi at: " + getRequestedGaz2Url);

        ResponseEntity<String> response = restTemplate.getForEntity(getRequestedGaz2Url, String.class);

        return response.getBody();
    }
}