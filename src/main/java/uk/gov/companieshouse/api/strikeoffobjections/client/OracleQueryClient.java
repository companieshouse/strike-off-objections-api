package uk.gov.companieshouse.api.strikeoffobjections.client;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OracleQueryClient {

    @Autowired
    private RestTemplate restTemplate;

    @Value("${ORACLE_QUERY_API_URL}")
    private String oracleQueryApiUrl;

    public Long getCompanyActionCode(String companyNumber) {
        String getCompanyActionCodeUrl = String.format("%s/company/%s/action-code", oracleQueryApiUrl, companyNumber);

        ResponseEntity<Long> response = restTemplate.getForEntity(getCompanyActionCodeUrl, Long.class);

        return response.getBody();
    }

    public String getRequestedGaz2(String companyNumber) {
        String getRequestedGaz2Url = String.format("%s/company/%s/gaz2-requested", oracleQueryApiUrl, companyNumber);

        ResponseEntity<String> response = restTemplate.getForEntity(getRequestedGaz2Url, String.class);

        return response.getBody();
    }
}
