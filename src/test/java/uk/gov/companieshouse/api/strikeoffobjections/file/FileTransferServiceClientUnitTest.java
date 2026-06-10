package uk.gov.companieshouse.api.strikeoffobjections.file;

import com.google.api.client.http.HttpResponseException;
import jakarta.servlet.ServletOutputStream;
import jakarta.servlet.WriteListener;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.filetransfer.FileApi;
import uk.gov.companieshouse.api.filetransfer.IdApi;
import uk.gov.companieshouse.api.handler.exception.URIValidationException;
import uk.gov.companieshouse.api.handler.filetransfer.InternalFileTransferClient;
import uk.gov.companieshouse.api.handler.filetransfer.PrivateFileTransferResourceHandler;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateFileTransferDelete;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateFileTransferDownload;
import uk.gov.companieshouse.api.handler.filetransfer.request.PrivateFileTransferUpload;
import uk.gov.companieshouse.api.model.ApiResponse;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

import java.io.IOException;
import java.util.function.Supplier;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class FileTransferServiceClientUnitTest {

    private static final String FILE_ID = "12345";

    @Mock
    private ApiLogger apiLogger;

    @Mock
    private InternalFileTransferClient internalClient;

    @Mock
    private PrivateFileTransferResourceHandler privateFileTransferResourceHandler;

    @Mock
    private MultipartFile multipartFile;

    @InjectMocks
    private FileTransferServiceClient fileTransferServiceClient;

    @BeforeEach
    void setup() {
        Supplier<InternalFileTransferClient> supplier = () -> internalClient;
        fileTransferServiceClient = new FileTransferServiceClient(apiLogger, supplier);
    }

    @Test
    void testUploadSuccess() throws Exception {
        var uploadBuilder = mock(PrivateFileTransferUpload.class);
        var idApi = new IdApi();
        idApi.setId("file123");
        var apiResponse = new ApiResponse<>(HttpStatus.OK.value(), null, idApi);

        when(multipartFile.getOriginalFilename()).thenReturn("test.pdf");

        when(internalClient.privateFileTransferHandler()).thenReturn(privateFileTransferResourceHandler);
        when(privateFileTransferResourceHandler.upload(any(), any(), any())).thenReturn(uploadBuilder);
        when(uploadBuilder.execute()).thenReturn(apiResponse);

        FileTransferApiClientResponse response = fileTransferServiceClient.upload(multipartFile);

        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals("file123", response.getFileId());
        verify(apiLogger).info(contains("upload(file=test.pdf) method called."));
    }

    @Test
    void testUploadNullResponse() throws Exception {
        var uploadBuilder = mock(PrivateFileTransferUpload.class);

        when(internalClient.privateFileTransferHandler()).thenReturn(privateFileTransferResourceHandler);
        when(privateFileTransferResourceHandler.upload(any(), any(), any())).thenReturn(uploadBuilder);
        when(uploadBuilder.execute()).thenReturn(null);

        FileTransferApiClientResponse response = fileTransferServiceClient.upload(multipartFile);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
        assertNull(response.getFileId());
        verify(apiLogger).errorContext(contains("null response from file transfer service url UPLOAD"), eq(null));
    }

    @Test
    void testUploadUriValidationException() throws Exception {
        var uploadBuilder = mock(PrivateFileTransferUpload.class);

        when(internalClient.privateFileTransferHandler()).thenReturn(privateFileTransferResourceHandler);
        when(privateFileTransferResourceHandler.upload(any(), any(), any())).thenReturn(uploadBuilder);
        when(uploadBuilder.execute()).thenThrow(new URIValidationException("bad uri"));

        assertThrows(HttpClientErrorException.class, () -> fileTransferServiceClient.upload(multipartFile));
        verify(apiLogger).errorContext(contains("uri validation failed from file transfer service url UPLOAD"), any(URIValidationException.class));
    }

    @Test
    void testUploadApiErrorResponseException() throws Exception {
        var uploadBuilder = mock(PrivateFileTransferUpload.class);

        when(internalClient.privateFileTransferHandler()).thenReturn(privateFileTransferResourceHandler);
        when(privateFileTransferResourceHandler.upload(any(), any(), any())).thenReturn(uploadBuilder);
        when(uploadBuilder.execute()).thenThrow(new ApiErrorResponseException(new HttpResponseException.Builder(HttpStatus.MOVED_PERMANENTLY.value(), "test", new com.google.api.client.http.HttpHeaders())));

        assertThrows(HttpServerErrorException.class, () -> fileTransferServiceClient.upload(multipartFile));
        verify(apiLogger).errorContext(contains("Api Error Response from file transfer service url"), any(ApiErrorResponseException.class));
    }

    @Test
    void testUploadIOException() throws Exception {
        when(multipartFile.getInputStream()).thenThrow(new IOException("io error"));

        assertThrows(HttpClientErrorException.class, () -> fileTransferServiceClient.upload(multipartFile));
    }

    @Test
    void testDeleteSuccess() throws Exception {
        var deleteBuilder = mock(PrivateFileTransferDelete.class);
        var apiResponse = new ApiResponse<Void>(HttpStatus.NO_CONTENT.value(), null, null);

        when(internalClient.privateFileTransferHandler()).thenReturn(privateFileTransferResourceHandler);
        when(privateFileTransferResourceHandler.delete(any())).thenReturn(deleteBuilder);
        when(deleteBuilder.execute()).thenReturn(apiResponse);

        FileTransferApiClientResponse response = fileTransferServiceClient.delete(FILE_ID);

        assertEquals(HttpStatus.NO_CONTENT, response.getHttpStatus());
        assertEquals(FILE_ID, response.getFileId());
        verify(apiLogger).info(contains("delete(fileId=" + FILE_ID + ") method called."));
    }

    @Test
    void testDeleteNullResponse() throws Exception {
        var deleteBuilder = mock(PrivateFileTransferDelete.class);

        when(internalClient.privateFileTransferHandler()).thenReturn(privateFileTransferResourceHandler);
        when(privateFileTransferResourceHandler.delete(any())).thenReturn(deleteBuilder);
        when(deleteBuilder.execute()).thenReturn(null);

        FileTransferApiClientResponse response = fileTransferServiceClient.delete(FILE_ID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
        assertEquals(FILE_ID, response.getFileId());
        verify(apiLogger).info(contains("delete(fileId=" + FILE_ID + ") method called."));
    }

    @Test
    void testDeleteUriValidationException() throws Exception {
        var deleteBuilder = mock(PrivateFileTransferDelete.class);

        when(internalClient.privateFileTransferHandler()).thenReturn(privateFileTransferResourceHandler);
        when(privateFileTransferResourceHandler.delete(any())).thenReturn(deleteBuilder);
        when(deleteBuilder.execute()).thenThrow(new URIValidationException("bad uri"));

        FileTransferApiClientResponse response = fileTransferServiceClient.delete(FILE_ID);

        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getHttpStatus());
        assertEquals(FILE_ID, response.getFileId());
        verify(apiLogger).errorContext(contains("uri validation failed from file transfer service url DELETE"), any(URIValidationException.class));
    }

    @Test
    void testDeleteApiErrorResponseException() throws Exception {
        var deleteBuilder = mock(PrivateFileTransferDelete.class);
        ApiErrorResponseException apiError = mock(ApiErrorResponseException.class);
        when(apiError.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());

        when(internalClient.privateFileTransferHandler()).thenReturn(privateFileTransferResourceHandler);
        when(privateFileTransferResourceHandler.delete(any())).thenReturn(deleteBuilder);
        when(deleteBuilder.execute()).thenThrow(apiError);

        assertThrows(HttpServerErrorException.class, () -> fileTransferServiceClient.delete(FILE_ID));
        verify(apiLogger).errorContext(contains("Api Error Response from file transfer service url DELETE"), eq(apiError));
    }

    @Test
    void testDownloadSuccess() throws Exception {
        MockHttpServletResponse servletResponse = spy(new MockHttpServletResponse());
        FileApi fileApi = new FileApi();
        fileApi.setFileName("file.pdf");
        fileApi.setMimeType("application/pdf");
        byte[] body = "hello".getBytes();
        fileApi.setBody(body);
        fileApi.setSize(body.length);

        ApiResponse<FileApi> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), Map.of(), fileApi);

        var downloadBuilder = mock(PrivateFileTransferDownload.class);
        when(internalClient.privateFileTransferHandler()).thenReturn(privateFileTransferResourceHandler);
        when(privateFileTransferResourceHandler.download(any())).thenReturn(downloadBuilder);
        when(downloadBuilder.execute()).thenReturn(apiResponse);

        fileTransferServiceClient.download(FILE_ID, servletResponse);

        verify(apiLogger).info(contains("download(fileId=" + FILE_ID + ") method called."));
        verify(apiLogger).info(contains("Download Complete[200]:"));
        verify(apiLogger).info(contains("setDownloadResponseHeaders(headers="));
        verify(apiLogger).debug(contains("Content-Type set using body data:"));
        verify(apiLogger).debug(contains("Content-Length set using body data:"));
        verify(apiLogger).debug(contains("Content-Disposition set using body data:"));
        verify(apiLogger).info(contains("buildDownloadResponse(filename=" + fileApi.getFileName() + ") method called."));
        assertEquals(HttpStatus.OK.value(), servletResponse.getStatus());
        assertEquals("application/pdf", servletResponse.getHeader("Content-Type"));
        assertEquals(String.valueOf(body.length), servletResponse.getHeader("Content-Length"));
        assertEquals("attachment; filename=\"file.pdf\"", servletResponse.getHeader("Content-Disposition"));
        assertEquals("hello", servletResponse.getContentAsString());
    }

    @Test
    void testDownloadUriValidationException() throws Exception {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();

        var downloadBuilder = mock(uk.gov.companieshouse.api.handler.filetransfer.request.PrivateFileTransferDownload.class);
        when(internalClient.privateFileTransferHandler()).thenReturn(privateFileTransferResourceHandler);
        when(privateFileTransferResourceHandler.download(any())).thenReturn(downloadBuilder);
        when(downloadBuilder.execute()).thenThrow(new URIValidationException("bad uri"));

        fileTransferServiceClient.download(FILE_ID, servletResponse);

        verify(apiLogger).errorContext(
                org.mockito.ArgumentMatchers.contains("uri validation failed from file transfer service url download"),
                any(URIValidationException.class));
        assertEquals(HttpStatus.BAD_REQUEST.value(), servletResponse.getStatus());
    }

    @Test
    void testDownloadApiErrorResponseException() throws Exception {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        var downloadBuilder = mock(uk.gov.companieshouse.api.handler.filetransfer.request.PrivateFileTransferDownload.class);
        ApiErrorResponseException apiError = mock(ApiErrorResponseException.class);
        when(apiError.getStatusCode()).thenReturn(HttpStatus.INTERNAL_SERVER_ERROR.value());

        when(internalClient.privateFileTransferHandler()).thenReturn(privateFileTransferResourceHandler);
        when(privateFileTransferResourceHandler.download(any())).thenReturn(downloadBuilder);
        when(downloadBuilder.execute()).thenThrow(apiError);

        fileTransferServiceClient.download(FILE_ID, servletResponse);

        verify(apiLogger).errorContext(
                org.mockito.ArgumentMatchers.contains("Api Error Response from file transfer service url download"),
                eq(apiError));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), servletResponse.getStatus());
    }

    @Test
    void testDownloadGenericException() throws Exception {
        MockHttpServletResponse servletResponse = new MockHttpServletResponse();
        var downloadBuilder = mock(uk.gov.companieshouse.api.handler.filetransfer.request.PrivateFileTransferDownload.class);

        when(internalClient.privateFileTransferHandler()).thenReturn(privateFileTransferResourceHandler);
        when(privateFileTransferResourceHandler.download(any())).thenReturn(downloadBuilder);
        when(downloadBuilder.execute()).thenThrow(new RuntimeException("fail"));

        fileTransferServiceClient.download(FILE_ID, servletResponse);

        verify(apiLogger).errorContext(
                org.mockito.ArgumentMatchers.contains("Api Error Response from file transfer service url download"),
                any(RuntimeException.class));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), servletResponse.getStatus());
    }

    @Test
    void testDownloadIOExceptionInBuildDownloadResponse() throws Exception {
        MockHttpServletResponse servletResponse = spy(new MockHttpServletResponse());
        FileApi fileApi = new FileApi();
        fileApi.setFileName("file.pdf");
        fileApi.setMimeType("application/pdf");
        byte[] body = "hello".getBytes();
        fileApi.setBody(body);
        fileApi.setSize(body.length);

        ApiResponse<FileApi> apiResponse = new ApiResponse<>(HttpStatus.OK.value(), Map.of(), fileApi);

        var downloadBuilder = mock(PrivateFileTransferDownload.class);
        when(internalClient.privateFileTransferHandler()).thenReturn(privateFileTransferResourceHandler);
        when(privateFileTransferResourceHandler.download(any())).thenReturn(downloadBuilder);
        when(downloadBuilder.execute()).thenReturn(apiResponse);

        ServletOutputStream servletOutputStream = new ServletOutputStream() {
            @Override
            public void write(int b) throws IOException {
                throw new IOException("io error");
            }
            @Override
            public boolean isReady() { return true; }
            @Override
            public void setWriteListener(WriteListener writeListener) { /* Empty Implementation as dummy */ }
        };

        doReturn(servletOutputStream).when(servletResponse).getOutputStream();

        fileTransferServiceClient.download(FILE_ID, servletResponse);

        verify(apiLogger).errorContext(
                org.mockito.ArgumentMatchers.contains("IO exception occurred from file transfer service url download"),
                any(IOException.class));
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR.value(), servletResponse.getStatus());
    }

}
