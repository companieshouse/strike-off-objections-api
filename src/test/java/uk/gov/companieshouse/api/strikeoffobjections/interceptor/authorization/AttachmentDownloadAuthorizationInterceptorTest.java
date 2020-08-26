package uk.gov.companieshouse.api.strikeoffobjections.interceptor.authorization;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

@Unit
@ExtendWith(MockitoExtension.class)
public class AttachmentDownloadAuthorizationInterceptorTest {
    
    @InjectMocks
    private AttachmentDownloadAuthorizationInterceptor interceptor;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ApiLogger logger;

    @Test
    public void willAuthoriseUserToDownloadAttachmentWhenOnlyDownloadRolePresent() {
        when(request.getHeader("X-Request-Id"))
                .thenReturn("123");
        when(request.getHeader("ERIC-Authorised-Roles"))
                .thenReturn("permission /admin/strike-off-objections-download");

        boolean result = interceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(request, times(1)).getHeader("ERIC-Authorised-Roles");
    }

    @Test
    public void willAuthoriseUserToDownloadAttachmentWhenDownloadRoleAndOthersArePresent() {
        when(request.getHeader("X-Request-Id"))
                .thenReturn("123");
        when(request.getHeader("ERIC-Authorised-Roles"))
                .thenReturn("permission /admin/another-role-not-for-download /admin/strike-off-objections-download /admin/yet-another-role-not-for-download");

        boolean result = interceptor.preHandle(request, response, null);

        assertTrue(result);
        verify(request, times(1)).getHeader("ERIC-Authorised-Roles");
    }

    @Test
    public void willNotAuthoriseUserToDownloadAttachmentWhenRoleMissing() {
        when(request.getHeader("X-Request-Id"))
                .thenReturn("123");
        when(request.getHeader("ERIC-Authorised-Roles"))
                .thenReturn("permission /admin/another-role-not-for-download");

        boolean result = interceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(request, times(1)).getHeader("ERIC-Authorised-Roles");
    }

    @Test
    public void willNotAuthoriseUserToDownloadAttachmentWhenListOfRolesIsMissingFromERICHeader() {
        boolean result = interceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(request, times(1)).getHeader("ERIC-Authorised-Roles");
    }
}
