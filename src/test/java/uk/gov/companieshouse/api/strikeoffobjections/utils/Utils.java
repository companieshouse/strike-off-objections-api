package uk.gov.companieshouse.api.strikeoffobjections.utils;

import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;

import java.io.IOException;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

public class Utils {

    public static final String ORIGINAL_FILE_NAME = "original.png";
    public static final String UPLOAD_ID = "5agf-g6hh";

    public static Objection getTestObjection(String id) {
        Objection objection = new Objection();
        objection.setId(id);
        return objection;
    }

    public static List<Attachment> getTestAttachments(String isContained) {
        List<Attachment> attachments = new ArrayList<Attachment>();
        attachments.add(buildTestAttachment("123", "test1.txt"));
        attachments.add(buildTestAttachment(isContained, "test2.txt"));
        attachments.add(buildTestAttachment("abc", "test3.txt"));
        return attachments;
    }

    public static Attachment buildTestAttachment(String id, String name) {
        Attachment attachment = new Attachment();
        attachment.setId(id);
        attachment.setName(name);
        return attachment;
    }

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

    public static FileTransferApiClientResponse getSuccessfulDeleteResponse() {
        FileTransferApiClientResponse fileTransferApiClientResponse = new FileTransferApiClientResponse();
        fileTransferApiClientResponse.setHttpStatus(HttpStatus.NO_CONTENT);
        return fileTransferApiClientResponse;
    }

    public static FileTransferApiClientResponse getUnsuccessfulFileTransferApiResponse() {
        FileTransferApiClientResponse fileTransferApiClientResponse = new FileTransferApiClientResponse();
        fileTransferApiClientResponse.setHttpStatus(HttpStatus.INTERNAL_SERVER_ERROR);
        return fileTransferApiClientResponse;
    }
}
