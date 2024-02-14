package uk.gov.companieshouse.api.strikeoffobjections.interceptor.authorization;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Optional;
import javax.validation.constraints.NotNull;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderFields;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderParser;
import uk.gov.companieshouse.service.ServiceException;

public class AttachmentDownloadAuthorizationInterceptor implements HandlerInterceptor {

    /**
     * The admin role that is assigned to CHS users who are allowed to download objection attachments.
     */
    private static final String ADMIN_DOWNLOAD_ROLE = "/admin/strike-off-objections-download";

    private final ApiLogger logger;
    private final ERICHeaderParser ericHeaderParser;

    public AttachmentDownloadAuthorizationInterceptor(
            ApiLogger logger, ERICHeaderParser ericHeaderParser) {
        this.logger = logger;
        this.ericHeaderParser = ericHeaderParser;
    }

    @Override
    public boolean preHandle(
            HttpServletRequest request, @NotNull HttpServletResponse response, @NotNull Object handler) {
        final String requestId = request.getHeader(ERICHeaderFields.ERIC_REQUEST_ID);

        logger.debugContext(requestId, "Check if user is authorized to download the attachment");

        if (isAttachmentDownloadAllowed(request, requestId)) {
            logger.debugContext(requestId, "User is authorized to download the attachment");

            return true;
        }

        final String user = request.getHeader(ERICHeaderFields.ERIC_AUTHORISED_USER);
        final String requestUserEmail = ericHeaderParser.getEmailAddress(user);

        logger.infoContext(
                requestId,
                String.format("User: %s is not authorized to download the attachment", requestUserEmail));
        response.setStatus(HttpStatus.UNAUTHORIZED.value());

        return false;
    }

    private boolean isAttachmentDownloadAllowed(HttpServletRequest request, String requestId) {
        try {
            if (userHasPrivilege(request, ADMIN_DOWNLOAD_ROLE, requestId)) {
                return true;
            }
        } catch (ServiceException e) {
            logger.errorContext(requestId, e);
        }

        return false;
    }

    private boolean userHasPrivilege(HttpServletRequest request, String privilege, String requestId)
            throws ServiceException {
        logger.debugContext(requestId, "Checking admin privileges for user");

        return Arrays.asList(
                        Optional.ofNullable(request.getHeader(ERICHeaderFields.ERIC_AUTHORISED_ROLES))
                                .orElseThrow(() -> new ServiceException(
                                        "Header missing: " + ERICHeaderFields.ERIC_AUTHORISED_ROLES))
                                .split(" "))
                .contains(privilege);
    }
}
