package uk.gov.companieshouse.api.strikeoffobjections.interceptor;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderFields;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Map;

public class ObjectionInterceptor implements HandlerInterceptor {

    private static final String OBJECTION_NOT_FOUND = "Objection not found";
    private static final String OBJECTION_STATUS_INVALID = "Objection is not in a valid state for this operation. Expected a status of OPEN but was %s";

    private final IObjectionService objectionService;
    private final ApiLogger apiLogger;

    public ObjectionInterceptor(IObjectionService objectionService, ApiLogger apiLogger) {
        this.objectionService = objectionService;
        this.apiLogger = apiLogger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String requestId = request.getHeader(ERICHeaderFields.ERIC_REQUEST_ID);
        Map<String, String> pathVariables =
                (Map<String, String>) request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE);

        final String objectionId = pathVariables.get(InterceptorConstants.OBJECTION_ID_PATH_VARIABLE);

        try {
            Objection objection = objectionService.getObjection(requestId, objectionId);
            
            // Operations on objections via the API REST interface are only allowed whilst the objection is
            // still 'open', i.e. has not yet been submitted (to CHIPS) for processing
            if (ObjectionStatus.OPEN != objection.getStatus()) {
                apiLogger.errorContext(
                        requestId,
                        String.format(OBJECTION_STATUS_INVALID, objection.getStatus()),
                        null
                );

                response.setStatus(HttpStatus.FORBIDDEN.value());
                return false;
            }
            
            request.setAttribute(InterceptorConstants.OBJECTION_ATTRIBUTE, objection);
        } catch (ObjectionNotFoundException e) {
            apiLogger.errorContext(
                    requestId,
                    OBJECTION_NOT_FOUND,
                    e
            );

            response.setStatus(HttpStatus.NOT_FOUND.value());
            return false;
        }
        return true;
    }
}
