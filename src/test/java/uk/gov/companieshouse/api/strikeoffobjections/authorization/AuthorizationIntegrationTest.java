package uk.gov.companieshouse.api.strikeoffobjections.authorization;

import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import javax.servlet.http.HttpServletResponse;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.RequestBuilder;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;

import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.controller.AttachmentMapper;
import uk.gov.companieshouse.api.strikeoffobjections.controller.ObjectionController;
import uk.gov.companieshouse.api.strikeoffobjections.controller.ObjectionMapper;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Integration;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ERICHeaderParser;
import uk.gov.companieshouse.api.strikeoffobjections.service.impl.ObjectionService;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.rest.response.PluggableResponseEntityFactory;

@Integration
@ExtendWith(SpringExtension.class)
@WebMvcTest(value = { ObjectionController.class })
public class AuthorizationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private ObjectionService objectionService;

    @MockBean
    private ERICHeaderParser headerParser;

    @MockBean
    private ObjectionMapper objectionMapper;

    @MockBean
    private AttachmentMapper attachmentMapper;

    @MockBean
    private ApiLogger logger;

    @MockBean
    private PluggableResponseEntityFactory responseEntityFactory;

    @BeforeEach
    public void setup() throws ServiceException {
        FileTransferApiClientResponse transferResponse = new FileTransferApiClientResponse();
        transferResponse.setFileId("123");
        transferResponse.setHttpStatus(HttpStatus.OK);
        when(objectionService.downloadAttachment(anyString(), anyString(), anyString(), any(HttpServletResponse.class)))
            .thenReturn(transferResponse);
    }

    @Test
    public void willNotAllowUserWithoutPermissionsToDownloadAttachment() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .get("/company/00006400/strike-off-objections/5f05c3f24be29647ef076f21/attachments/123/download")
            .accept(MediaType.APPLICATION_JSON)
            .header("X-Request-Id", "444");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }

    @Test
    public void willAllowUserWithDownloadRoleToDownloadAttachment() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .get("/company/00006400/strike-off-objections/5f05c3f24be29647ef076f21/attachments/123/download")
            .accept(MediaType.APPLICATION_JSON)
            .header("X-Request-Id", "444")
            .header("ERIC-Authorised-Scope", "")
            .header("ERIC-Authorised-Roles", "/admin/strike-off-objections-download");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(HttpStatus.OK.value(), result.getResponse().getStatus());
    }

    @Test
    public void willNotAllowUserWithoutDownloadRoleToDownloadAttachment() throws Exception {
        RequestBuilder requestBuilder = MockMvcRequestBuilders
            .get("/company/00006400/strike-off-objections/5f05c3f24be29647ef076f21/attachments/123/download")
            .accept(MediaType.APPLICATION_JSON)
            .header("ERIC-Authorised-Scope", "")
            .header("ERIC-Authorised-Roles", "/admin/some-other-role");

        MvcResult result = mockMvc.perform(requestBuilder).andReturn();
        assertEquals(HttpStatus.UNAUTHORIZED.value(), result.getResponse().getStatus());
    }
}