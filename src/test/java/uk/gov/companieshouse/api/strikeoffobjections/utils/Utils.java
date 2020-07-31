package uk.gov.companieshouse.api.strikeoffobjections.utils;

import com.fasterxml.jackson.core.JsonProcessingException;
import org.apache.avro.Schema;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.email.EmailContent;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class Utils {

    public static final String ORIGINAL_FILE_NAME = "original.png";
    public static final String UPLOAD_ID = "5agf-g6hh";

    public static Objection getSimpleTestObjection(String objectionId){
        Objection objection = new Objection();
        objection.setId(objectionId);
        return objection;
    }

    public static Objection getTestObjection(String objectionId,
                                             String reason,
                                             String companyNumber,
                                             String userId,
                                             String email
                                             ) {
        Objection objection = new Objection();
        objection.setReason(reason);
        objection.setId(objectionId);
        objection.setCompanyNumber(companyNumber);
        CreatedBy createdBy = new CreatedBy(userId, email);
        objection.setCreatedBy(createdBy);

        return objection;
    }

    public static List<Attachment> getTestAttachments(){
        Attachment attachment1 = new Attachment();
        Attachment attachment2 = new Attachment();
        attachment1.setName("Name 1");
        attachment2.setName("Name 2");

        return Arrays.asList(
                attachment1, attachment2
        );
    }

    public static List<Attachment> getTestAttachmentsContainingKey(String keyContained) {
        List<Attachment> attachments = new ArrayList<Attachment>();
        attachments.add(buildTestAttachment("123", "test1.txt"));
        attachments.add(buildTestAttachment(keyContained, "test2.txt"));
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


    public static EmailContent buildEmailContent(String appId, String messageId, String messageType,
                                                 Map<String, Object> data, String recipient,
                                                 LocalDateTime createdAt) {
        return new EmailContent.Builder()
            .withOriginatingAppId(appId)
            .withMessageId(messageId)
            .withMessageType(messageType)
            .withData(data)
            .withEmailAddress(recipient)
            .withCreatedAt(createdAt)
            .build();
    }

    public static Map<String, Object> getDummyEmailData() {
        Map<String, Object> data = new HashMap<>();
        data.put("to", "example@test.co.uk");
        data.put("subject", "Test objection submitted");
        data.put("company_name", "TEST COMPANY");
        data.put("company_number", "00001111");
        data.put("reason", "Testing this");
        return data;
    }

    public static Schema getDummySchema(URL url) throws IOException {
        String avroSchemaPath = Objects.requireNonNull(url).getFile();
        Schema.Parser parser = new Schema.Parser();
        return parser.parse(new File(avroSchemaPath));
    }

    public static CompanyProfileApi getDummyCompanyProfile(String companyNumber, String jurisdiction) {
        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyNumber(companyNumber);
        companyProfileApi.setCompanyName("Company: " + companyNumber);
        companyProfileApi.setJurisdiction(jurisdiction);
        return companyProfileApi;
    }
}
