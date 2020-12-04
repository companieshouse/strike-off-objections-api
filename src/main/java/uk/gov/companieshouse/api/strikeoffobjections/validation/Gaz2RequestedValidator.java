package uk.gov.companieshouse.api.strikeoffobjections.validation;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.client.OracleQueryClient;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.LogConstants;
import uk.gov.companieshouse.api.strikeoffobjections.model.eligibility.EligibilityStatus;

import java.util.HashMap;
import java.util.Map;

@Component
public class Gaz2RequestedValidator {

    private OracleQueryClient oracleQueryClient;
    private ApiLogger apiLogger;

    @Value("${GAZ_1_ACTION_CODE}")
    private long gaz1ActionCode;

    @Autowired
    public Gaz2RequestedValidator(OracleQueryClient oracleQueryClient, ApiLogger apiLogger) {
        this.oracleQueryClient = oracleQueryClient;
        this.apiLogger = apiLogger;
    }

    public void validate(String companyNumber, Long actionCode, String logContext) throws ValidationException{
        if (gaz1ActionCode == actionCode) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LogConstants.COMPANY_NUMBER.getValue(), companyNumber);
            logMap.put(LogConstants.ACTION_CODE.getValue(), actionCode);

            apiLogger.debugContext(logContext, "Company action code is GAZ1, checking for requested GAZ2");

            String requestedGaz2Result = oracleQueryClient.getRequestedGaz2(companyNumber, logContext);

            if (requestedGaz2Result != null) {
                apiLogger.infoContext(logContext, "Company has a requested GAZ2 transaction ,failing validation", logMap);
                throw new ValidationException(EligibilityStatus.INELIGIBLE_GAZ2_REQUESTED);
            }

            apiLogger.debugContext(logContext, "Company does not have requested GAZ2 transaction, passing validation");
        }
    }
}
