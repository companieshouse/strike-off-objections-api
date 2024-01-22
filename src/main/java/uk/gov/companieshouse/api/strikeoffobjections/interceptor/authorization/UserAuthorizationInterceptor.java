package uk.gov.companieshouse.api.strikeoffobjections.interceptor.authorization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.interceptor.InterceptorConstants;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderFields;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderParser;

public class UserAuthorizationInterceptor implements HandlerInterceptor {

    private final ApiLogger apiLogger;
    private final ERICHeaderParser ericHeaderParser;

    public UserAuthorizationInterceptor(ApiLogger apiLogger, ERICHeaderParser ericHeaderParser) {
        this.apiLogger = apiLogger;
        this.ericHeaderParser = ericHeaderParser;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        boolean userIsAuthorised = false;

        final String requestId = request.getHeader(ERICHeaderFields.ERIC_REQUEST_ID);
        apiLogger.debugContext(requestId, "Checking current user is authorised to access objection");
        final String user = request.getHeader(ERICHeaderFields.ERIC_AUTHORISED_USER);

        final Objection objection = (Objection) request.getAttribute(InterceptorConstants.OBJECTION_ATTRIBUTE);

        final String createdByUserEmail = objection.getCreatedBy().getEmail();
        final String requestUserEmail = ericHeaderParser.getEmailAddress(user);

        if(createdByUserEmail.equals(requestUserEmail)) {
            apiLogger.debugContext(requestId, "User is authorised to access objection");
            userIsAuthorised = true;
        } else {
            apiLogger.infoContext(requestId, String.format("User: %s not authorised to access objection %s",
                    requestUserEmail, objection.getId()));
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
        }
        return userIsAuthorised;
    }
}
