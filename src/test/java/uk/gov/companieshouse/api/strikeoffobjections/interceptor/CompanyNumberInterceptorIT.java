package uk.gov.companieshouse.api.strikeoffobjections.interceptor;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.BeforeEach;
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
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Integration;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderParser;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ObjectionService;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

@Integration
@ExtendWith(SpringExtension.class)
@WebMvcTest(value = { ObjectionController.class })
class CompanyNumberInterceptorIT {
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

    @BeforeEach
    public void setup() throws ObjectionNotFoundException {
        Objection objection = new Objection();
        objection.setStatus(ObjectionStatus.OPEN);
        objection.setCompanyNumber("00006400");
        CreatedBy createdBy = new CreatedBy("some id", "demo@ch.gov.uk",
                "client", "Joe Bloggs", false);
        objection.setCreatedBy(createdBy);
        when(objectionService.getObjection(any(), any())).thenReturn(objection);
        when(headerParser.getEmailAddress(any())).thenReturn("demo@ch.gov.uk");
    }

    @Test
    void willAllowRequestWithMatchingCompanyNumbersToExecute() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/company/00006400/strike-off-objections/5f05c3f24be29647ef076f21")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Request-Id", "444");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    void willBlockRequestWithoutMatchingCompanyNumbers() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
                .get("/company/00000099/strike-off-objections/5f05c3f24be29647ef076f21")
                .accept(MediaType.APPLICATION_JSON)
                .header("X-Request-Id", "444");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(HttpStatus.BAD_REQUEST.value(), result.getResponse().getStatus());
    }
}
