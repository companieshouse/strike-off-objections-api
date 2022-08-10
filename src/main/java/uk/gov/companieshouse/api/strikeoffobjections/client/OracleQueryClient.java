package uk.gov.companieshouse.api.strikeoffobjections.client;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.UnsafeUrlException;

import java.util.List;

import static uk.gov.companieshouse.api.strikeoffobjections.exception.UnsafeUrlException.ExceptionType.UNSAFE_COMPANY_NUMBER;

@Component
public class OracleQueryClient {

    @Autowired
    private RestTemplate restTemplate;

    @Autowired
    private ApiLogger apiLogger;

    @Value("${ORACLE_QUERY_API_URL}")
    private String oracleQueryApiUrl;

    @Value("${VALID_COMPANY_NUMBER_REGEX:")  // default to empty list to retain backward compatibility
    private List<String> validCompanyNumberRegEx;

    public Long getCompanyActionCode(String companyNumber, String requestId) {

        String getCompanyActionCodeUrl = String.format("%s/company/%s/action-code", oracleQueryApiUrl, companyNumber);
        apiLogger.infoContext(requestId, "Calling Oracle Query APi at: " + getCompanyActionCodeUrl);

        if (isDangerous(companyNumber)) {
            throw new UnsafeUrlException(UNSAFE_COMPANY_NUMBER, companyNumber);
        }

        ResponseEntity<Long> response = restTemplate.getForEntity(getCompanyActionCodeUrl, Long.class);

        return response.getBody();
    }

    public String getRequestedGaz2(String companyNumber, String requestId) {
        String getRequestedGaz2Url = String.format("%s/company/%s/gaz2-requested", oracleQueryApiUrl, companyNumber);
        apiLogger.infoContext(requestId, "Calling Oracle Query APi at: " + getRequestedGaz2Url);

        if (isDangerous(companyNumber)) {
            throw new UnsafeUrlException(UNSAFE_COMPANY_NUMBER, companyNumber);
        }

        ResponseEntity<String> response = restTemplate.getForEntity(getRequestedGaz2Url, String.class);

        return response.getBody();
    }

    private boolean isDangerous(String companyNumber) {
        if (CollectionUtils.isEmpty(validCompanyNumberRegEx)) {
            // If there is no setup, revert to behaviour as before
            return false;
        }
        return validCompanyNumberRegEx
                .stream()
                .filter(regEx -> regEx.startsWith("^"))
                .filter(regEx -> regEx.endsWith("$"))
                .noneMatch(companyNumber::matches);
    }
}
