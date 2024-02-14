package uk.gov.companieshouse.api.strikeoffobjections.interceptor.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderParser;

@Unit
@ExtendWith(MockitoExtension.class)
class AttachmentDownloadAuthorizationInterceptorTest {

    @InjectMocks private AttachmentDownloadAuthorizationInterceptor interceptor;

    @Mock private HttpServletRequest request;

    @Mock private HttpServletResponse response;

    @Mock private ApiLogger logger;

    @Mock private ERICHeaderParser ericHeaderParser;

    Object object = new Object();

    @Test
    void willAuthoriseUserToDownloadAttachmentWhenOnlyDownloadRolePresent() {
        when(request.getHeader("X-Request-Id")).thenReturn("123");
        when(request.getHeader("ERIC-Authorised-Roles"))
                .thenReturn("permission /admin/strike-off-objections-download");

        boolean result = interceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(request, times(1)).getHeader("ERIC-Authorised-Roles");
    }

    @Test
    void willAuthoriseUserToDownloadAttachmentWhenDownloadRoleAndOthersArePresent() {
        when(request.getHeader("X-Request-Id")).thenReturn("123");
        when(request.getHeader("ERIC-Authorised-Roles"))
                .thenReturn(
                        "permission /admin/another-role-not-for-download /admin/strike-off-objections-download"
                                + " /admin/yet-another-role-not-for-download");

        boolean result = interceptor.preHandle(request, response, object);

        assertTrue(result);
        verify(request, times(1)).getHeader("ERIC-Authorised-Roles");
    }

    @Test
    void willNotAuthoriseUserToDownloadAttachmentWhenRoleMissing() {
        when(request.getHeader("X-Request-Id")).thenReturn("123");
        when(request.getHeader("ERIC-Authorised-Roles"))
                .thenReturn("permission /admin/another-role-not-for-download");

        boolean result = interceptor.preHandle(request, response, object);

        assertFalse(result);
        verify(request, times(1)).getHeader("ERIC-Authorised-Roles");
    }

    @Test
    void willNotAuthoriseUserToDownloadAttachmentWhenListOfRolesIsMissingFromERICHeader() {
        boolean result = interceptor.preHandle(request, response, object);

        assertFalse(result);
        verify(request, times(1)).getHeader("ERIC-Authorised-Roles");
    }
}
