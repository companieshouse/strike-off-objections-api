package uk.gov.companieshouse.api.strikeoffobjections.interceptor;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;

@Unit
@ExtendWith(MockitoExtension.class)
class ObjectionInterceptorTest {

    private static final String OBJECTION_ID = "OBJECTION";
    private static Map<String, String> PATH_VARIABLES;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @Mock
    private ApiLogger apiLogger;

    @Mock
    private IObjectionService objectionService;

    @InjectMocks
    private ObjectionInterceptor objectionInterceptor;

    @BeforeEach
    void init() {
        PATH_VARIABLES = new HashMap<>();
        PATH_VARIABLES.put("objectionId", OBJECTION_ID);
    }

    @Test
    void testObjectionInterceptor() throws Exception {
        Objection objection = new Objection();
        objection.setStatus(ObjectionStatus.OPEN);

        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .thenReturn(PATH_VARIABLES);
        when(objectionService.getObjection(any(), any())).thenReturn(objection);

        boolean result = objectionInterceptor.preHandle(request, response, null);
        assertTrue(result);
        verify(request, times(1)).setAttribute("objection", objection);
    }

    @Test
    void testObjectionInterceptorObjectionNotFound() throws Exception {
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE))
                .thenReturn(PATH_VARIABLES);
        when(objectionService.getObjection(any(), any()))
                .thenThrow(new ObjectionNotFoundException("Not found"));

        boolean result = objectionInterceptor.preHandle(request, response, null);
        assertFalse(result);
        verify(response, times(1)).setStatus(HttpStatus.NOT_FOUND.value());
    }
}
