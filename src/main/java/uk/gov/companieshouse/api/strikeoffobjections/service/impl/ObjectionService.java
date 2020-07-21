package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;

import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.LogConstants;
import uk.gov.companieshouse.api.strikeoffobjections.exception.AttachmentNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClient;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.api.strikeoffobjections.file.ObjectionsLinkKeys;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.model.patcher.ObjectionPatcher;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;
import uk.gov.companieshouse.api.strikeoffobjections.repository.ObjectionRepository;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.links.Links;

import javax.validation.constraints.NotNull;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class ObjectionService implements IObjectionService {

    private static final String OBJECTION_NOT_FOUND_MESSAGE = "Objection with id: %s, not found";
    private static final String ATTACHMENT_NOT_FOUND_MESSAGE = "Attachment with id: %s, not found";
    private static final String DELETE_ERROR_MESSAGE = "Unable to delete attachment %s, status code %s";
    private static final String DELETE_ERROR_MESSAGE_SHORT = "Unable to delete attachment %s";

    private ObjectionRepository objectionRepository;
    private ApiLogger logger;
    private Supplier<LocalDateTime> dateTimeSupplier;
    private ObjectionPatcher objectionPatcher;
    private FileTransferApiClient fileTransferApiClient;
    private ERICHeaderParser ericHeaderParser;

    @Autowired
    public ObjectionService(ObjectionRepository objectionRepository,
            ApiLogger logger,
            Supplier<LocalDateTime> dateTimeSupplier,
            ObjectionPatcher objectionPatcher,
            FileTransferApiClient fileTransferApiClient,
            ERICHeaderParser ericHeaderParser) {
        this.objectionRepository = objectionRepository;
        this.logger = logger;
        this.dateTimeSupplier = dateTimeSupplier;
        this.objectionPatcher = objectionPatcher;
        this.fileTransferApiClient = fileTransferApiClient;
        this.ericHeaderParser = ericHeaderParser;
    }

    @Override
    public String createObjection(String requestId, String companyNumber, String ericUserId, String ericUserDetails) throws Exception{
        Map<String, Object> logMap = buildLogMap(companyNumber, null);
        logger.infoContext(requestId, "Creating objection", logMap);

        final String userEmailAddress = ericHeaderParser.getEmailAddress(ericUserDetails);
        
        Objection entity = new Objection.Builder()
                .withCompanyNumber(companyNumber)
                .withCreatedOn(dateTimeSupplier.get())
                .withCreatedBy(new CreatedBy(ericUserId, userEmailAddress))
                .withHttpRequestId(requestId)
                .withStatus(ObjectionStatus.OPEN)
                .build();

        Objection savedEntity = objectionRepository.save(entity);
        return savedEntity.getId();
    }

    @Override
    public void patchObjection(String requestId, String companyNumber, String objectionId, ObjectionPatch objectionPatch) throws ObjectionNotFoundException {
        Map<String, Object> logMap = buildLogMap(companyNumber, objectionId);
        logger.infoContext(requestId, "Checking for existing objection", logMap);

        Optional<Objection> existingObjection = objectionRepository.findById(objectionId);
        if (existingObjection.isPresent()) {
            logger.infoContext(requestId, "Objection exists, patching", logMap);
            Objection objection = objectionPatcher.patchObjection(objectionPatch, requestId, existingObjection.get());
            objectionRepository.save(objection);
        } else {
            logger.infoContext(requestId, "Objection does not exist", logMap);
            throw new ObjectionNotFoundException(String.format(OBJECTION_NOT_FOUND_MESSAGE, objectionId));
        }
    }

    @Override
    public ServiceResult<String> addAttachment(String requestId, String objectionId, MultipartFile file, String attachmentsUri)
            throws ServiceException, ObjectionNotFoundException {
        Map<String, Object> logMap = buildLogMap(null, objectionId);
        logger.infoContext(requestId, "Uploading attachments", logMap);
        FileTransferApiClientResponse response = fileTransferApiClient.upload(requestId, file);
        logger.infoContext(requestId, "Finished uploading attachments", logMap);

        HttpStatus responseHttpStatus = response.getHttpStatus();
        if (responseHttpStatus != null && responseHttpStatus.isError()) {
            throw new ServiceException(responseHttpStatus.toString());
        }
        String attachmentId = response.getFileId();
        if (StringUtils.isBlank(attachmentId)) {
            throw new ServiceException("No file id returned from file upload");
        } else {
            Attachment attachment = createAttachment(file, attachmentId);
            Objection objection = objectionRepository.findById(objectionId).orElseThrow(
                    () -> new ObjectionNotFoundException(String.format(OBJECTION_NOT_FOUND_MESSAGE, objectionId))
            );
            objection.addAttachment(attachment);

            Links links = createLinks(attachmentsUri, attachmentId);
            attachment.setLinks(links);

            objectionRepository.save(objection);

            return ServiceResult.accepted(attachmentId);
        }
    }

    @Override
    public Attachment getAttachment(
            String requestId,
            String companyNumber,
            String objectionId,
            String attachmentId
    ) throws ObjectionNotFoundException, AttachmentNotFoundException {
        Objection objection = objectionRepository.findById(objectionId).orElseThrow(
                () -> new ObjectionNotFoundException(String.format(OBJECTION_NOT_FOUND_MESSAGE, objectionId))
        );

        List<Attachment> attachments = objection.getAttachments();
        return attachments.parallelStream().filter(o -> attachmentId.equals(o.getId())).findFirst().orElseThrow(
                () -> new AttachmentNotFoundException(String.format(ATTACHMENT_NOT_FOUND_MESSAGE, attachmentId))
        );
    }

    private Links createLinks(String attachmentsUri, String attachmentId) {
        String linkToSelf = attachmentsUri + "/" + attachmentId;
        Links links = new Links();
        links.setLink(ObjectionsLinkKeys.SELF, linkToSelf);
        links.setLink(ObjectionsLinkKeys.DOWNLOAD, linkToSelf + "/download");
        return links;
    }

    private Attachment createAttachment(@NotNull MultipartFile file, String attachmentId) {
        Attachment attachment = new Attachment();
        attachment.setId(attachmentId);
        String filename = file.getOriginalFilename();
        attachment.setName(filename);
        attachment.setSize(file.getSize());
        attachment.setContentType(file.getContentType());
        return attachment;
    }

    @Override
    public List<Attachment> getAttachments(String requestId, String companyNumber, String objectionId) throws ObjectionNotFoundException {
        Map<String, Object> logMap = buildLogMap(companyNumber, objectionId);
        logger.infoContext(requestId, "Finding the objection", logMap);

        Optional<Objection> objection = objectionRepository.findById(objectionId);
        if (objection.isPresent()) {
            logger.infoContext(requestId, "Objection exists, returning attachments", logMap);
            return objection.get().getAttachments();
        } else {
            logger.infoContext(requestId, "Objection does not exist", logMap);
            throw new ObjectionNotFoundException(String.format(OBJECTION_NOT_FOUND_MESSAGE, objectionId));
        }
    }

    @Override
    public void deleteAttachment(String requestId, String companyNumber, String objectionId, String attachmentId)
            throws ObjectionNotFoundException, AttachmentNotFoundException, ServiceException {

        Objection objection = objectionRepository.findById(objectionId).orElseThrow(
                () -> new ObjectionNotFoundException(String.format(OBJECTION_NOT_FOUND_MESSAGE, objectionId))
        );

        List<Attachment> attachments = objection.getAttachments();
        Attachment attachment = attachments.parallelStream().filter(o -> attachmentId.equals(o.getId())).findFirst().orElseThrow(
                () -> new AttachmentNotFoundException(String.format(ATTACHMENT_NOT_FOUND_MESSAGE, attachmentId))
        );

        Map<String, Object> logMap = buildLogMap(companyNumber, objectionId);
        deleteFromS3(requestId, attachmentId, logMap);

        attachments.remove(attachment);

        objection.setAttachments(attachments);

        objectionRepository.save(objection);

    }

    private void deleteFromS3(String requestId, String attachmentId, Map<String, Object>  logMap) throws ServiceException {
        try {
            FileTransferApiClientResponse response = fileTransferApiClient.delete(requestId, attachmentId);
            if (response == null || response.getHttpStatus() == null) {
                String message = String.format(DELETE_ERROR_MESSAGE_SHORT, attachmentId);
                logger.infoContext(requestId, message, logMap);
                throw new ServiceException(message);
            } else {
                if (response.getHttpStatus().isError()) {
                    String message = String.format(DELETE_ERROR_MESSAGE,
                            attachmentId, response.getHttpStatus());
                    logger.infoContext(requestId, message, logMap);
                    throw new ServiceException(message);
                }
            }
        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String message = String.format(DELETE_ERROR_MESSAGE, attachmentId, e.getStatusCode());
            logger.errorContext(requestId, message, e, logMap);
            throw new ServiceException(message);
        }
    }

    private Map<String, Object> buildLogMap(String companyNumber, String objectionId) {
        Map<String, Object> logMap = new HashMap<>();
        if(StringUtils.isNotBlank(companyNumber)) {
            logMap.put(LogConstants.COMPANY_NUMBER.getValue(), companyNumber);
        }
        if(StringUtils.isNotBlank(objectionId)) {
            logMap.put(LogConstants.OBJECTION_ID.getValue(), objectionId);
        }
        return logMap;
    }
}
