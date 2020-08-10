package uk.gov.companieshouse.api.strikeoffobjections.file;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;

import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class FileTransferApiClient {

    private static final String HEADER_API_KEY = "x-api-key";
    private static final String UPLOAD = "upload";
    private static final String CONTENT_DISPOSITION_VALUE = "form-data; name=%s; filename=%s";
    private static final String NULL_RESPONSE_MESSAGE = "null response from file transfer api url";
    private static final String DELETE_URI = "%s/%s";

    @Autowired
    private ApiLogger logger;

    @Autowired
    private RestTemplate restTemplate;

    @Value("${FILE_TRANSFER_API_URL}")
    private String fileTransferApiURL;

    @Value("${FILE_TRANSFER_API_KEY}")
    private String fileTransferApiKey;


    private <T> FileTransferApiClientResponse makeApiCall(String requestId,
                                                          FileTransferOperation <T> operation,
                                                          FileTransferResponseBuilder <T> responseBuilder) {
        FileTransferApiClientResponse response = new FileTransferApiClientResponse();

        try {
            T operationResponse = operation.execute();
            response = responseBuilder.createResponse(operationResponse);
        } catch (IOException e) {
            logger.errorContext(requestId, e.getMessage(), e);
            response.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return response;
    }

    /**
     * Uploads a file to the file-transfer-api
     * Creates a multipart form request containing the file and sends to
     * the file-transfer-api. The response from the file-transfer-api contains
     * the new unique id for the file. This is captured and returned in the FileTransferApiClientResponse.
     * @param fileToUpload The file to upload
     * @return FileTransferApiClientResponse containing the file id if successful, the http header and http status
     */
    public FileTransferApiClientResponse upload(String requestId, MultipartFile fileToUpload) {

        return makeApiCall(
             requestId,
             () -> getFileTransferOperation(fileToUpload),
             responseEntity ->  getFileTransferApiClientResponse(requestId, responseEntity)
        );
    }

    /**
     * Calls the file transfer api and returns the result of the
     * call ready to pass to the response builder
     * @param fileToUpload multipart file to be uploaded
     * @return ResponseEntity containing the raw data from which the response object is built
     * @throws IOException when multipart file bytes have access errors
     */
    private ResponseEntity<FileTransferApiResponse> getFileTransferOperation(MultipartFile fileToUpload) throws IOException {
        HttpHeaders headers = createFileTransferApiHttpHeaders();
        LinkedMultiValueMap<String, String> fileHeaderMap = createUploadFileHeader(fileToUpload);
        HttpEntity<byte[]> fileHttpEntity = new HttpEntity<>(fileToUpload.getBytes(), fileHeaderMap);
        LinkedMultiValueMap<String, Object> body = createUploadBody(fileHttpEntity);
        HttpEntity<LinkedMultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
        return restTemplate.postForEntity(fileTransferApiURL, requestEntity, FileTransferApiResponse.class);
    }

    /**
     * FileTransferResponseBuilder - the output from FileTransferOperation is the input into
     * this FileTransferResponseBuilder
     * @param responseEntity raw data returned from file transfer api upload call
     * @return FileTransferApiClientResponse contains data ready to add to mongo
     */
    private FileTransferApiClientResponse getFileTransferApiClientResponse(
            String requestId, ResponseEntity<FileTransferApiResponse> responseEntity) {
        FileTransferApiClientResponse fileTransferApiClientResponse = new FileTransferApiClientResponse();
        if (responseEntity != null) {
            fileTransferApiClientResponse.setHttpHeaders(responseEntity.getHeaders());
            fileTransferApiClientResponse.setHttpStatus(responseEntity.getStatusCode());
            FileTransferApiResponse apiResponse = responseEntity.getBody();
            if (apiResponse != null) {
                fileTransferApiClientResponse.setFileId(apiResponse.getId());
            }
        } else {
            logger.infoContext(requestId, String.format("%s %s", NULL_RESPONSE_MESSAGE,fileTransferApiURL));
            fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        }
        return fileTransferApiClientResponse;
    }

    private HttpHeaders createFileTransferApiHttpHeaders() {
        HttpHeaders headers = createApiKeyHeader();
        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
        return headers;
    }

    private HttpHeaders createApiKeyHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HEADER_API_KEY, fileTransferApiKey);
        return headers;
    }

    private LinkedMultiValueMap<String, String> createUploadFileHeader(MultipartFile fileToUpload) {
        LinkedMultiValueMap<String, String> fileHeaderMap = new LinkedMultiValueMap<>();
        fileHeaderMap.add(HttpHeaders.CONTENT_DISPOSITION, String.format(CONTENT_DISPOSITION_VALUE, UPLOAD, fileToUpload.getOriginalFilename()));
        return fileHeaderMap;
    }

    private LinkedMultiValueMap<String, Object> createUploadBody(HttpEntity<byte[]> fileHttpEntity) {
        LinkedMultiValueMap<String, Object> multipartReqMap = new LinkedMultiValueMap<>();
        multipartReqMap.add(UPLOAD, fileHttpEntity);
        return multipartReqMap;
    }

    /**
     * Delete a file from S3 via the file-transfer-api
     * @param fileId of document to be deleted
     * @return FileTransferApiClientResponse containing the http status
     */
    public FileTransferApiClientResponse delete(String requestId, String fileId) {
        String deleteUrl = String.format(DELETE_URI, fileTransferApiURL, fileId);
        return makeApiCall(
            requestId,
            () -> {
                HttpEntity<Void> request = new HttpEntity<>(createApiKeyHeader());
                return restTemplate.exchange(deleteUrl, HttpMethod.DELETE, request, String.class);
            },
            responseEntity -> {
                FileTransferApiClientResponse response = new FileTransferApiClientResponse();
                response.setHttpStatus(responseEntity.getStatusCode());
                return response;
            }
        );
    }

    /**
     * Downloads a file from the file-transfer-api
     * The RestTemplate execute method takes a callback function to handle the response
     * from the file-transfer-api. it's in here that we copy the data coming in from
     * the file-transfer-api into the provided outputStream.
     * @param fileId The id used by the file-transfer-api to identify the file
     * @param httpServletResponse The HttpServletResponse to stream the file to
     * @return FileTransferApiClientResponse containing the http status
     */
    public FileTransferApiClientResponse download(String fileId, HttpServletResponse httpServletResponse) {
        // TODO OBJ-200 replace this dummy response with the implementation.
        FileTransferApiClientResponse fileTransferApiClientResponse = new FileTransferApiClientResponse();
        fileTransferApiClientResponse.setFileId(fileId);
        fileTransferApiClientResponse.setHttpStatus(HttpStatus.OK);
        return fileTransferApiClientResponse;
    }
}
