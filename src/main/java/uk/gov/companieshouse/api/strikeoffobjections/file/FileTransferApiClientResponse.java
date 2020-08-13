package uk.gov.companieshouse.api.strikeoffobjections.file;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;

public class FileTransferApiClientResponse {

    private String fileId;

    private HttpStatus httpStatus;
    private HttpHeaders httpHeaders;

    public String getFileId() {
        return fileId;
    }

    public void setFileId(String fileId) {
        this.fileId = fileId;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public void setHttpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
    }
}
