package uk.gov.companieshouse.api.strikeoffobjections.interceptor.authorization;

import java.util.Arrays;
import java.util.Optional;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderFields;
import uk.gov.companieshouse.service.ServiceException;

public class AttachmentDownloadAuthorizationInterceptor extends HandlerInterceptorAdapter {

    /**
     * The admin role that is assigned to CHS users who are allowed to download objection attachments.
     */
    private static final String ADMIN_DOWNLOAD_ROLE = "/admin/strike-off-objections-download";

    private ApiLogger logger;


    public AttachmentDownloadAuthorizationInterceptor(ApiLogger logger) {
        this.logger = logger;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        final String requestId = request.getHeader(ERICHeaderFields.ERIC_REQUEST_ID);

        logger.debugContext(requestId, "Check if user is authorized to download the attachment");

        if (isAttachmentDownloadAllowed(request, requestId)) {
            logger.debugContext(requestId, "User is authorized to download the attachment");

            return true;
        }

        logger.errorContext(requestId, "User is not authorized to download the attachment", null);
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        return false;
    }

    private boolean isAttachmentDownloadAllowed(HttpServletRequest request, String requestId) {
       try {
           if (userHasPrivilege(request, ADMIN_DOWNLOAD_ROLE, requestId)) {
                return true;
            }
        } catch(ServiceException e) {
            logger.errorContext(requestId, e);
        }

        return false;
    }

    private boolean userHasPrivilege(HttpServletRequest request, String privilege, String requestId) throws ServiceException {
        logger.debugContext(requestId, "Checking admin privileges for user");

        return Arrays.stream(
                Optional.ofNullable(request.getHeader(ERICHeaderFields.ERIC_AUTHORISED_ROLES))
                        .orElseThrow(() -> new ServiceException("Header missing: " + ERICHeaderFields.ERIC_AUTHORISED_ROLES))
                        .split(" "))
                .anyMatch(privilege::equals);
    }
}

