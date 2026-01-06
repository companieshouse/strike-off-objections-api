package uk.gov.companieshouse.api.strikeoffobjections.interceptor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.controller.AttachmentMapper;
import uk.gov.companieshouse.api.strikeoffobjections.controller.ObjectionController;
import uk.gov.companieshouse.api.strikeoffobjections.controller.ObjectionMapper;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Integration;
import uk.gov.companieshouse.api.strikeoffobjections.interceptor.authorization.UserAuthorizationInterceptor;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderParser;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ObjectionService;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Integration
@ExtendWith(SpringExtension.class)
@WebMvcTest(value = { ObjectionController.class })
class EligibilityIT {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private ObjectionService objectionService;

    @MockitoBean
    private ERICHeaderParser headerParser;

    @MockitoBean
    private ObjectionMapper objectionMapper;

    @MockitoBean
    private AttachmentMapper attachmentMapper;

    @MockitoBean
    private ApiLogger logger;

    @MockitoBean
    private PluggableResponseEntityFactory responseEntityFactory;

    @MockitoBean
    private ObjectionInterceptor objectionInterceptor;

    @MockitoBean
    private CompanyNumberInterceptor companyNumberInterceptor;

    @MockitoBean
    private UserAuthorizationInterceptor userAuthorizationInterceptor;

    @Test
    void interceptorsNotCalledForEligibilityEndpoint() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/company/00006400/strike-off-objections/eligibility")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Request-Id", "444");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        verify(objectionInterceptor, times(0)).preHandle(any(), any(), any());
        verify(companyNumberInterceptor, times(0)).preHandle(any(), any(), any());
        verify(userAuthorizationInterceptor, times(0)).preHandle(any(), any(), any());
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }
}
