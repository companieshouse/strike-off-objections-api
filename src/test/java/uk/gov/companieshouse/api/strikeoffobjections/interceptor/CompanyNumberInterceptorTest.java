package uk.gov.companieshouse.api.strikeoffobjections.interceptor;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.servlet.HandlerMapping;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class CompanyNumberInterceptorTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String WRONG_COMPANY_NUMBER = "87654321";
    private static Map<String, String> PATH_VARIABLES;

    @Mock
    private ApiLogger apiLogger;

    @Mock
    private HttpServletRequest request;

    @Mock
    private HttpServletResponse response;

    @InjectMocks
    private CompanyNumberInterceptor companyNumberInterceptor;

    @BeforeEach
    void init() {
        PATH_VARIABLES = new HashMap<>();
        PATH_VARIABLES.put("companyNumber", COMPANY_NUMBER);
    }

    @Test
    void companyNumbersMatch() throws Exception {
        Objection objection = new Objection();
        objection.setCompanyNumber(COMPANY_NUMBER);
        when(request.getAttribute("objection")).thenReturn(objection);
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(PATH_VARIABLES);
        boolean result = companyNumberInterceptor.preHandle(request, response, null);

        assertTrue(result);
    }

    @Test
    void companyNumbersDoNotMatch() throws Exception {
        Objection objection = new Objection();
        objection.setCompanyNumber(WRONG_COMPANY_NUMBER);
        when(request.getAttribute("objection")).thenReturn(objection);
        when(request.getAttribute(HandlerMapping.URI_TEMPLATE_VARIABLES_ATTRIBUTE)).thenReturn(PATH_VARIABLES);
        boolean result = companyNumberInterceptor.preHandle(request, response, null);

        assertFalse(result);
        verify(response, times(1)).setStatus(HttpStatus.BAD_REQUEST.value());
    }
}