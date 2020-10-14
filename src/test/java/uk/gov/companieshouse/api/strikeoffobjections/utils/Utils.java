package uk.gov.companieshouse.api.strikeoffobjections.utils;

import org.apache.avro.Schema;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.api.strikeoffobjections.model.create.ObjectionCreate;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.email.EmailContent;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionLinkKeys;
import uk.gov.companieshouse.service.links.Links;

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
                                             String email,
                                             LocalDateTime localDatetime,
                                             ObjectionCreate objectionCreate) {

        Objection objection = new Objection();
        objection.setReason(reason);
        objection.setId(objectionId);
        objection.setCompanyNumber(companyNumber);
        CreatedBy createdBy = new CreatedBy(userId, email,
                objectionCreate.getFullName(), objectionCreate.canShareIdentity());
        objection.setCreatedBy(createdBy);
        objection.setCreatedOn(localDatetime);

        return objection;
    }

    public static ObjectionCreate buildTestObjectionCreate(String fullName,
                                                           boolean shareIdentity) {
        ObjectionCreate objectionCreate = new ObjectionCreate();
        objectionCreate.setFullName(fullName);
        objectionCreate.setShareIdentity(shareIdentity);
        return objectionCreate;
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

    public static void setTestAttachmentsWithLinks(List<Attachment> attachments) {
        Attachment attachment1 = Utils.buildTestAttachment("id1", "TestAttachment1");
        Links links1 = new Links();
        links1.setLink(ObjectionLinkKeys.DOWNLOAD, "/url1/download");
        attachment1.setLinks(links1);
        attachments.add(attachment1);
        Attachment attachment2 = Utils.buildTestAttachment("id1", "TestAttachment2");
        Links links2 = new Links();
        links2.setLink(ObjectionLinkKeys.DOWNLOAD, "/url2/download");
        attachment2.setLinks(links2);
        attachments.add(attachment2);
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
        data.put("full_name", "Joe Bloggs");
        data.put("share_identity", false);
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

    public static FileTransferApiClientResponse dummyDownloadResponse() {
        FileTransferApiClientResponse dummyDownloadResponse = new FileTransferApiClientResponse();
        dummyDownloadResponse.setHttpStatus(HttpStatus.OK);
        return dummyDownloadResponse;
    }

    public static HttpHeaders getDummyHttpHeaders(ContentDisposition contentDisposition,
                                                  int contentLength,
                                                  MediaType contentType) {
        //create dummy headers that would be returned from calling the file-transfer-api
        HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.setContentLength(contentLength);
        httpHeaders.setContentDisposition(contentDisposition);
        httpHeaders.setContentType(contentType);
        return httpHeaders;
    }
}
