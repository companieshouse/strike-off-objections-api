package uk.gov.companieshouse.api.strikeoffobjections.authorization;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderFields;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderParser;
import uk.gov.companieshouse.service.ServiceException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UserAuthorizationInterceptor implements HandlerInterceptor {

    private final ApiLogger apiLogger;
    private final  IObjectionService objectionService;
    private final ERICHeaderParser ericHeaderParser;

    public UserAuthorizationInterceptor(ApiLogger apiLogger, IObjectionService objectionService, ERICHeaderParser ericHeaderParser) {
        this.apiLogger = apiLogger;
        this.objectionService = objectionService;
        this.ericHeaderParser = ericHeaderParser;

    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        final String requestId = request.getHeader(ERICHeaderFields.ERIC_REQUEST_ID);
        apiLogger.debugContext(requestId, "Checking current user is authorised for objection id");
        final String user = request.getHeader(ERICHeaderFields.ERIC_AUTHORISED_USER);
        final String objectionId = getObjectionId(request.getRequestURI());

        final Objection objection = objectionService.getObjection(requestId, objectionId);

        final String createdByUserEmail = objection.getCreatedBy().getEmail();
        final String requestUserEmail = ericHeaderParser.getEmailAddress(user);

        if(createdByUserEmail.equals(requestUserEmail)) {
            apiLogger.debugContext(requestId, "User is authorised for objection id");
            return true;
        }

        apiLogger.debugContext(requestId, "User not authorised for objection id");
        response.setStatus(HttpStatus.UNAUTHORIZED.value());
        return false;
    }

    private String getObjectionId(String requestURI) throws ServiceException{
        String[] splitURI = requestURI.split("/");
        boolean nextIsObjectionId = false;
        for(String segment : splitURI) {

            if (nextIsObjectionId) {
                return segment;
            }
            if ("strike-off-objections".equals(segment)) {
                nextIsObjectionId = true;
            }
        }
        throw new ServiceException("No ObjectionId found in request URI");
    }
}
