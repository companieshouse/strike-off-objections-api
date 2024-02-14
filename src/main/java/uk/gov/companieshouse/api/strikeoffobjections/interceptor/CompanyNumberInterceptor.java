package uk.gov.companieshouse.api.strikeoffobjections.interceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Map;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderFields;

public class CompanyNumberInterceptor implements HandlerInterceptor {

    private final ApiLogger apiLogger;

    public CompanyNumberInterceptor(ApiLogger apiLogger) {
        this.apiLogger = apiLogger;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, HttpServletResponse response, Object handler) {
        final String requestId = request.getHeader(ERICHeaderFields.ERIC_REQUEST_ID);
        apiLogger.debugContext(
                requestId, "Checking provided company number matches objection company number");
        final Objection objection =
                (Objection) request.getAttribute(InterceptorConstants.OBJECTION_ATTRIBUTE);

        Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        final String companyNumber =
                pathVariables.get(InterceptorConstants.COMPANY_NUMBER_PATH_VARIABLE);
        final boolean companyNumberMatches = doCompanyNumbersMatch(companyNumber, objection);

        if (!companyNumberMatches) {
            apiLogger.infoContext(
                    requestId, "Provided company number does not match objection company number");
            response.setStatus(HttpStatus.BAD_REQUEST.value());
        }

        apiLogger.debugContext(requestId, "Provided company number matches objection company number");
        return companyNumberMatches;
    }

    private boolean doCompanyNumbersMatch(String requestCompanyNumber, Objection objection) {
        boolean companyNumberMatches = false;
        if (requestCompanyNumber != null) {
            companyNumberMatches = requestCompanyNumber.equals(objection.getCompanyNumber());
        }
        return companyNumberMatches;
    }
}
