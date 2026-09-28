package uk.gov.companieshouse.api.strikeoffobjections.file;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpServer;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import uk.gov.companieshouse.api.handler.filetransfer.FileTransferHttpClient;
import uk.gov.companieshouse.api.handler.filetransfer.InternalFileTransferClient;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Integration;

@Integration
class FileTransferServiceClientIT {

    private static final String FILE_NAME = "Test.docx";
    private static final String FILE_CONTENT_TYPE =
            "application/vnd.openxmlformats-officedocument.wordprocessingml.document";
    private static final byte[] FILE_CONTENT = "document-content".getBytes(StandardCharsets.UTF_8);
    private static final Pattern BOUNDARY_PATTERN = Pattern.compile("boundary=\"?([^\";]+)");

    private final AtomicReference<CapturedRequest> capturedRequest = new AtomicReference<>();

    private HttpServer server;
    private FileTransferServiceClient fileTransferServiceClient;

    @BeforeEach
    void setUp() throws IOException {
        server = HttpServer.create(new InetSocketAddress(0), 0);
        server.createContext("/file-transfer-service/", this::captureUpload);
        server.start();

        var httpClient = new FileTransferHttpClient("test-api-key");
        var internalClient = new InternalFileTransferClient(httpClient);
        internalClient.setBasePath("http://localhost:" + server.getAddress().getPort());

        fileTransferServiceClient =
                new FileTransferServiceClient(mock(ApiLogger.class), () -> internalClient);
    }

    @AfterEach
    void tearDown() {
        server.stop(0);
    }

    @Test
    void uploadSendsFileTransferApiCompatibleMultipartRequest() {
        var file = new MockMultipartFile(
                "file",
                FILE_NAME,
                FILE_CONTENT_TYPE,
                FILE_CONTENT);

        FileTransferApiClientResponse response = fileTransferServiceClient.upload(file);

        assertEquals(HttpStatus.OK, response.getHttpStatus());
        assertEquals("file-id", response.getFileId());

        CapturedRequest request = capturedRequest.get();
        assertNotNull(request);
        assertEquals("POST", request.method());
        assertNotNull(
                request.contentType(),
                "The file-transfer API will reject the upload because Content-Type is missing");
        assertTrue(
                request.contentType().startsWith("multipart/form-data"),
                () -> "Unexpected Content-Type: " + request.contentType());

        Matcher boundaryMatcher = BOUNDARY_PATTERN.matcher(request.contentType());
        assertTrue(
                boundaryMatcher.find(),
                () -> "Multipart boundary missing from Content-Type: " + request.contentType());

        String boundary = boundaryMatcher.group(1);
        String lowerCaseBody = request.body().toLowerCase(Locale.ROOT);
        assertTrue(request.body().contains("--" + boundary));
        assertTrue(lowerCaseBody.contains(
                "content-disposition: form-data; name=\"file\"; filename=\""
                        + FILE_NAME.toLowerCase(Locale.ROOT) + "\""));
        assertTrue(lowerCaseBody.contains("content-type: " + FILE_CONTENT_TYPE));
        assertTrue(request.body().contains(new String(FILE_CONTENT, StandardCharsets.UTF_8)));
        assertTrue(request.body().contains("--" + boundary + "--"));
    }

    private void captureUpload(HttpExchange exchange) throws IOException {
        byte[] requestBody = exchange.getRequestBody().readAllBytes();
        capturedRequest.set(new CapturedRequest(
                exchange.getRequestMethod(),
                exchange.getRequestHeaders().getFirst("Content-Type"),
                new String(requestBody, StandardCharsets.ISO_8859_1)));

        byte[] responseBody = "{\"id\":\"file-id\"}".getBytes(StandardCharsets.UTF_8);
        exchange.getResponseHeaders().set("Content-Type", "application/json");
        exchange.sendResponseHeaders(HttpStatus.OK.value(), responseBody.length);
        exchange.getResponseBody().write(responseBody);
        exchange.close();
    }

    private record CapturedRequest(String method, String contentType, String body) {
    }
}
