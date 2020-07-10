package uk.gov.companieshouse.api.strikeoffobjections.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClientResponse;

import java.io.IOException;
import java.nio.file.Files;

public class Utils {

    public static final String ORIGINAL_FILE_NAME = "original.png";
    public static final String UPLOAD_ID = "5agf-g6hh";

    public static MultipartFile mockMultipartFile() throws IOException {
        String fileName = "testMultipart.txt";
        Resource rsc = new ClassPathResource("input/testMultipart.txt");
        return new MockMultipartFile(fileName,
                ORIGINAL_FILE_NAME, "text/plain", Files.readAllBytes(rsc.getFile().toPath()));
    }

    public static FileTransferApiClientResponse getSuccessfulUploadResponse() {
        FileTransferApiClientResponse fileTransferApiClientResponse = new FileTransferApiClientResponse();
        fileTransferApiClientResponse.setFileId(UPLOAD_ID);
        return fileTransferApiClientResponse;
    }

    public static FileTransferApiClientResponse getUnsuccessfulUploadResponse() {
        FileTransferApiClientResponse fileTransferApiClientResponse = new FileTransferApiClientResponse();
        fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return fileTransferApiClientResponse;
    }
}
