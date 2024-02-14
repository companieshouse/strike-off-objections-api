package uk.gov.companieshouse.api.strikeoffobjections.interceptor.authorization;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderParser;

@Unit
@ExtendWith(MockitoExtension.class)
class UserAuthorizationInterceptorTest {

    private static final String USER_EMAIL = "demo@ch.gov.uk";
    private static final String DIFFERENT_USER_EMAIL = "different@ch.gov.uk";

    @Mock private ApiLogger apiLogger;

    @Mock private ERICHeaderParser ericHeaderParser;

    @Mock private HttpServletRequest request;

    @Mock private HttpServletResponse response;

    @InjectMocks private UserAuthorizationInterceptor userAuthorizationInterceptor;

    @Test
    void testUserAuthorised() {

        Objection objection = new Objection();
        CreatedBy createdBy = new CreatedBy("id", USER_EMAIL, "client", "Joe Bloggs", false);
        objection.setCreatedBy(createdBy);
        when(ericHeaderParser.getEmailAddress(any())).thenReturn(USER_EMAIL);
        when(request.getAttribute("objection")).thenReturn(objection);

        boolean result = userAuthorizationInterceptor.preHandle(request, response, null);

        InOrder logOrder = inOrder(apiLogger);
        logOrder
                .verify(apiLogger)
                .debugContext(eq(null), eq("Checking current user is authorised to access objection"));
        logOrder.verify(apiLogger).debugContext(eq(null), eq("User is authorised to access objection"));

        assertTrue(result);
    }

    @Test
    void testUserNotAuthorised() {
        Objection objection = new Objection();
        CreatedBy createdBy = new CreatedBy("id", USER_EMAIL, "client", "Joe Bloggs", false);
        objection.setCreatedBy(createdBy);
        when(ericHeaderParser.getEmailAddress(any())).thenReturn(DIFFERENT_USER_EMAIL);
        when(request.getAttribute("objection")).thenReturn(objection);

        boolean result = userAuthorizationInterceptor.preHandle(request, response, null);
        InOrder logOrder = inOrder(apiLogger);
        logOrder
                .verify(apiLogger)
                .debugContext(eq(null), eq("Checking current user is authorised to access objection"));
        logOrder
                .verify(apiLogger)
                .infoContext(
                        eq(null), eq("User: different@ch.gov.uk not authorised to access objection null"));

        assertFalse(result);
        verify(response, times(1)).setStatus(401);
    }
}
