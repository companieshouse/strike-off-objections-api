package uk.gov.companieshouse.api.strikeoffobjections.interceptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class ObjectionStatusInterceptorTest {

    private static final String OBJECTION_ATTRIBUTE = "objection";

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ApiLogger apiLogger;

    @InjectMocks
    private ObjectionStatusInterceptor objectionStatusInterceptor;

    @Test
    void testObjectionInterceptorObjectionNoLongerOpen() throws Exception {
        Objection objection = new Objection();
        objection.setStatus(ObjectionStatus.PROCESSED);

        when(request.getAttribute(OBJECTION_ATTRIBUTE)).thenReturn(objection);

        boolean result = objectionStatusInterceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(response, times(1)).setStatus(HttpStatus.FORBIDDEN.value());
    }

    @Test
    void testObjectionInterceptorObjectionOpen() throws Exception {
        Objection objection = new Objection();
        objection.setStatus(ObjectionStatus.OPEN);

        when(request.getAttribute(OBJECTION_ATTRIBUTE)).thenReturn(objection);

        boolean result = objectionStatusInterceptor.preHandle(request, response, null);

        assertTrue(result);
        verifyNoInteractions(response);
    }
}
