package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.platform.commons.util.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.LogConstants;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
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

    private ObjectionRepository objectionRepository;
    private ApiLogger logger;
    private Supplier<LocalDateTime> dateTimeSupplier;
    private ObjectionPatcher objectionPatcher;
    private FileTransferApiClient fileTransferApiClient;

    @Autowired
    public ObjectionService(ObjectionRepository objectionRepository,
                            ApiLogger logger,
                            Supplier<LocalDateTime> dateTimeSupplier,
                            ObjectionPatcher objectionPatcher,
                            FileTransferApiClient fileTransferApiClient) {
        this.objectionRepository = objectionRepository;
        this.logger = logger;
        this.dateTimeSupplier = dateTimeSupplier;
        this.objectionPatcher = objectionPatcher;
        this.fileTransferApiClient = fileTransferApiClient;
    }

    @Override
    public String createObjection(String requestId, String companyNumber) throws Exception{
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), companyNumber);
        logger.infoContext(requestId, "Creating objection", logMap);

        Objection entity = new Objection.Builder()
                .withCompanyNumber(companyNumber)
                .withCreatedOn(dateTimeSupplier.get())
                .withHttpRequestId(requestId)
                .withStatus(ObjectionStatus.OPEN)
                .build();

        Objection savedEntity = objectionRepository.save(entity);
        return savedEntity.getId();
    }

    @Override
    public void patchObjection(String requestId, String companyNumber, String objectionId, ObjectionPatch objectionPatch) throws ObjectionNotFoundException {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), companyNumber);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), objectionId);
        logger.infoContext(requestId, "Checking for existing objection", logMap);

        Optional<Objection> existingObjection = objectionRepository.findById(objectionId);
        if (existingObjection.isPresent()) {
            logger.infoContext(requestId, "Objection exists, patching", logMap);
            Objection objection = objectionPatcher.patchObjection(objectionPatch, requestId, existingObjection.get());
            objectionRepository.save(objection);
        } else {
            logger.infoContext(requestId, "Objection does not exist", logMap);
            throw new ObjectionNotFoundException(String.format("Objection with id: %s, not found", objectionId));
        }
    }

    @Override
    public ServiceResult<String> addAttachment(String requestId, String objectionId, MultipartFile file, String attachmentsUri)
            throws ServiceException, ObjectionNotFoundException {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.OBJECTION_ID.getValue(), objectionId);
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
                    () -> new ObjectionNotFoundException(String.format("Objection with id: %s, not found", objectionId))
            );
            objection.addAttachment(attachment);

            Links links = createLinks(attachmentsUri, attachmentId);
            attachment.setLinks(links);

            objectionRepository.save(objection);

            return ServiceResult.accepted(attachmentId);
        }
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
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), companyNumber);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), objectionId);
        logger.infoContext(requestId, "Finding the objection", logMap);

        Optional<Objection> objection = objectionRepository.findById(objectionId);
        if (objection.isPresent()) {
            logger.infoContext(requestId, "Objection exists, returning attachments", logMap);
            return objection.get().getAttachments();
        } else {
            logger.infoContext(requestId, "Objection does not exist", logMap);
            throw new ObjectionNotFoundException(String.format("Objection with id: %s, not found", objectionId));
        }
    }
}
