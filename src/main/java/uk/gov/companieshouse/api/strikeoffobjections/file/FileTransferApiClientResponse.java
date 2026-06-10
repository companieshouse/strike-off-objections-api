package uk.gov.companieshouse.api.strikeoffobjections.file;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;

import static org.springframework.http.HttpStatus.valueOf;

public class FileTransferApiClientResponse {

    private String fileId;
    private HttpStatus httpStatus;
    private HttpHeaders httpHeaders;

    public String getFileId() {
        return fileId;
    }

    public FileTransferApiClientResponse fileId(String fileId) {
        this.fileId = fileId;
        return this;
    }

    public HttpStatus getHttpStatus() {
        return httpStatus;
    }

    public void setHttpStatus(HttpStatus httpStatus) {
        this.httpStatus = httpStatus;
    }

    public FileTransferApiClientResponse httpStatus(HttpStatusCode httpStatusCode) {
        this.httpStatus = valueOf(httpStatusCode.value());
        return this;
    }


    public HttpHeaders getHttpHeaders() {
        return httpHeaders;
    }

    public FileTransferApiClientResponse httpHeaders(HttpHeaders httpHeaders) {
        this.httpHeaders = httpHeaders;
        return this;
    }
}
