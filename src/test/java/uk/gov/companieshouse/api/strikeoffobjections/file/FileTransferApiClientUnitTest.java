package uk.gov.companieshouse.api.strikeoffobjections.file;

import org.apache.commons.lang.ArrayUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.platform.commons.util.StringUtils;
import org.mockito.ArgumentCaptor;
import org.mockito.ArgumentMatchers;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RequestCallback;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class FileTransferApiClientUnitTest {

    private static final String REQUEST_ID = "abc";
    private static final String DUMMY_URL = "http://test";
    private static final String FILE_ID = "12345";
    private static final String EXCEPTION_MESSAGE = "BAD THINGS";
    private static final String DELETE_URL_TEMPLATE = DUMMY_URL + "/{fileId}";
    private static final String DOWNLOAD_URI_TEMPLATE = DUMMY_URL + "/{fileId}/download";

    @Captor
    private ArgumentCaptor<ResponseExtractor<ClientHttpResponse>> responseExtractorArgCaptor;

    @Mock
    private RestTemplate restTemplate;

    @Mock
    private ApiLogger apiLogger;

    @InjectMocks
    private FileTransferApiClient fileTransferApiClient;

    private MultipartFile file;

    @BeforeEach
    void setup() {
        ReflectionTestUtils.setField(fileTransferApiClient, "fileTransferApiURL", DUMMY_URL);
        file = new MockMultipartFile("testFile", new byte[10]);
        fileTransferApiClient.init();
    }

    @Test
    void testUploadSuccess() {
        final ResponseEntity<FileTransferApiResponse> apiResponse = apiSuccessResponse();

        when(restTemplate.postForEntity(eq(DUMMY_URL), any(), eq(FileTransferApiResponse.class)))
                .thenReturn(apiResponse);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferApiClient.upload(REQUEST_ID, file);

        assertEquals(FILE_ID, fileTransferApiClientResponse.getFileId());
        assertEquals(HttpStatus.OK, fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    void testUploadApiReturnsError() {
        final ResponseEntity<FileTransferApiResponse> apiErrorResponse = apiErrorResponse();

        when(restTemplate.postForEntity(eq(DUMMY_URL), any(), eq(FileTransferApiResponse.class))).thenReturn(apiErrorResponse);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferApiClient.upload(REQUEST_ID, file);

        assertTrue(fileTransferApiClientResponse.getHttpStatus().isError());
        assertEquals(apiErrorResponse.getStatusCode(), fileTransferApiClientResponse.getHttpStatus());
        assertTrue(StringUtils.isBlank(fileTransferApiClientResponse.getFileId()));
    }

    @Test
    void testUploadGenericExceptionResponse() {
        final RestClientException exception = new RestClientException(EXCEPTION_MESSAGE);

        when(restTemplate.postForEntity(eq(DUMMY_URL), any(), eq(FileTransferApiResponse.class))).thenThrow(exception);

        RestClientException thrown = assertThrows(RestClientException.class, () -> fileTransferApiClient.upload(REQUEST_ID, file));
        assertEquals(exception.getMessage(), thrown.getMessage());
    }

    @Test
    void testDeleteSuccess() {
        final ResponseEntity<String> apiResponse = new ResponseEntity<>("", HttpStatus.NO_CONTENT);
        when(restTemplate.exchange(eq(DELETE_URL_TEMPLATE), eq(HttpMethod.DELETE), any(), eq(String.class), anyMap()))
                .thenReturn(apiResponse);
        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferApiClient.delete(REQUEST_ID, FILE_ID);
        assertEquals(HttpStatus.NO_CONTENT, fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    void testDeleteApiReturnsError() {
        final ResponseEntity<String> apiResponse = new ResponseEntity<>("", HttpStatus.INTERNAL_SERVER_ERROR);

        when(restTemplate.exchange(eq(DELETE_URL_TEMPLATE), eq(HttpMethod.DELETE), any(), eq(String.class), anyMap()))
                .thenReturn(apiResponse);

        FileTransferApiClientResponse fileTransferApiClientResponse = fileTransferApiClient.delete(REQUEST_ID, FILE_ID);

        assertTrue(fileTransferApiClientResponse.getHttpStatus().isError());
        assertEquals(apiResponse.getStatusCode(), fileTransferApiClientResponse.getHttpStatus());
    }

    @Test
    void testDeleteGenericExceptionResponse() {
        final RestClientException exception = new RestClientException(EXCEPTION_MESSAGE);

        when(restTemplate.exchange(eq(DELETE_URL_TEMPLATE), eq(HttpMethod.DELETE), any(), eq(String.class), anyMap())).thenThrow(exception);

        RestClientException thrown = assertThrows(RestClientException.class, () -> fileTransferApiClient.delete(REQUEST_ID, FILE_ID));
        assertEquals(exception.getMessage(), thrown.getMessage());
    }

    @Test
    void testSuccessfulDownload() throws IOException {
        final String contentDispositionType = "attachment";
        final int contentLength = 55123;
        final MediaType contentType = MediaType.APPLICATION_OCTET_STREAM;
        final String fileName = "file.txt";
        final File file = new File("./src/test/resources/input/test.txt");
        final InputStream fileInputStream = new FileInputStream(file);

        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        ClientHttpResponse responseFromFileTransferApi = Mockito.mock(ClientHttpResponse.class);

        ContentDisposition contentDisposition = ContentDisposition.builder(contentDispositionType)
                .filename(fileName).build();

        HttpHeaders httpHeaders = Utils.getDummyHttpHeaders(contentDisposition, contentLength, contentType);

        //tell mocks what to return when download method is executed
        when(restTemplate.execute(eq(DOWNLOAD_URI_TEMPLATE), eq(HttpMethod.GET), any(RequestCallback.class), ArgumentMatchers.<ResponseExtractor<ClientHttpResponse>>any(), anyMap()))
                .thenReturn(responseFromFileTransferApi);
        when(responseFromFileTransferApi.getBody()).thenReturn(fileInputStream);
        when(responseFromFileTransferApi.getStatusCode()).thenReturn(HttpStatus.OK);
        when(responseFromFileTransferApi.getHeaders()).thenReturn(httpHeaders);

        FileTransferApiClientResponse downloadResponse = fileTransferApiClient.download(REQUEST_ID, FILE_ID, servletResponse);

        //need to capture the responseExtractor lambda passed to the restTemplate so we can test it - this is what actually does the file copy
        verify(restTemplate).execute(eq(DOWNLOAD_URI_TEMPLATE), eq(HttpMethod.GET), any(RequestCallback.class),
                responseExtractorArgCaptor.capture(), anyMap());

        //now executing the responseExtractor should cause input stream (file) to be copied to output stream (servletResponse)
        ResponseExtractor<ClientHttpResponse> responseExtractor = responseExtractorArgCaptor.getValue();
        responseExtractor.extractData(responseFromFileTransferApi);

        //check status is ok
        assertEquals(HttpStatus.OK, downloadResponse.getHttpStatus());
        assertTrue(ArrayUtils.isEquals(Files.readAllBytes(file.toPath()), servletResponse.getContentAsByteArray()));

        assertEquals(contentType.toString(), servletResponse.getHeader("Content-Type"));
        assertEquals(String.valueOf(contentLength), servletResponse.getHeader("Content-Length"));
        assertEquals(contentDisposition.toString(), servletResponse.getHeader("Content-Disposition"));
    }

    @Test
    void testNullClientHttpResponseForDownload() {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        //tell mocks what to return when download method is executed
        when(restTemplate.execute(eq(DOWNLOAD_URI_TEMPLATE), eq(HttpMethod.GET), any(RequestCallback.class), ArgumentMatchers.<ResponseExtractor<ClientHttpResponse>>any(), anyMap()))
                .thenReturn(null);

        FileTransferApiClientResponse downloadResponse = fileTransferApiClient.download(REQUEST_ID, FILE_ID, servletResponse);

        //need to capture the responseExtractor lambda passed to the restTemplate so we can test it - this is what actually does the file copy
        verify(restTemplate, times(1)).execute(eq(DOWNLOAD_URI_TEMPLATE), eq(HttpMethod.GET), any(RequestCallback.class),
                ArgumentMatchers.<ResponseExtractor<ClientHttpResponse>>any(), anyMap());

        //check status is Internal Server Error
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, downloadResponse.getHttpStatus());
    }

    @Test
    void testGenericExceptionThrownWhenDownloadCalled() {
        final MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        final RestClientException exception = new RestClientException(EXCEPTION_MESSAGE);

        when(restTemplate.execute(eq(DOWNLOAD_URI_TEMPLATE),
                eq(HttpMethod.GET),
                any(RequestCallback.class),
                ArgumentMatchers.<ResponseExtractor<ClientHttpResponse>>any(),
                anyMap()))
                .thenThrow(exception);

        RestClientException thrown = assertThrows(RestClientException.class, () -> fileTransferApiClient.download(REQUEST_ID, FILE_ID, servletResponse));
        assertEquals(exception.getMessage(), thrown.getMessage());
    }

    private ResponseEntity<FileTransferApiResponse> apiSuccessResponse() {
        FileTransferApiResponse response = new FileTransferApiResponse();
        response.setId(FILE_ID);
        return new ResponseEntity<>(response, HttpStatus.OK);
    }

    private ResponseEntity<FileTransferApiResponse> apiErrorResponse() {
        FileTransferApiResponse response = new FileTransferApiResponse();
        return new ResponseEntity<>(response, HttpStatus.BAD_REQUEST);
    }
}
