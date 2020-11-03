package uk.gov.companieshouse.api.strikeoffobjections.interceptor;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderFields;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class ObjectionStatusInterceptor implements HandlerInterceptor {

    private static final String OBJECTION_STATUS_INVALID = "Objection is not in a valid state for this operation. Expected a status of OPEN but was %s";

    private final ApiLogger apiLogger;

    public ObjectionStatusInterceptor(ApiLogger apiLogger) {
        this.apiLogger = apiLogger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        final String requestId = request.getHeader(ERICHeaderFields.ERIC_REQUEST_ID);
        apiLogger.debugContext(requestId, "Checking provided company number matches objection company number");
        final Objection objection = (Objection) request.getAttribute(InterceptorConstants.OBJECTION_ATTRIBUTE);

        // Operations on objections via the API REST interface are only allowed whilst the objection is
        // still 'open', i.e. has not yet been submitted (to CHIPS) for processing
        if (ObjectionStatus.OPEN != objection.getStatus()) {
            apiLogger.infoContext(
                    requestId,
                    String.format(OBJECTION_STATUS_INVALID, objection.getStatus())
            );

            response.setStatus(HttpStatus.FORBIDDEN.value());
            return false;
        }
        return true;
    }
}
