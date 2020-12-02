package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.multipart.MultipartFile;

import uk.gov.companieshouse.api.strikeoffobjections.client.OracleQueryClient;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.LogConstants;
import uk.gov.companieshouse.api.strikeoffobjections.exception.AttachmentNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.exception.InvalidObjectionStatusException;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClient;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.api.strikeoffobjections.file.ObjectionsLinkKeys;
import uk.gov.companieshouse.api.strikeoffobjections.model.create.ObjectionCreate;
import uk.gov.companieshouse.api.strikeoffobjections.model.eligibility.ObjectionEligibility;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;
import uk.gov.companieshouse.api.strikeoffobjections.model.patcher.ObjectionPatcher;
import uk.gov.companieshouse.api.strikeoffobjections.processor.ObjectionProcessor;
import uk.gov.companieshouse.api.strikeoffobjections.repository.ObjectionRepository;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;
import uk.gov.companieshouse.api.strikeoffobjections.service.IReferenceNumberGeneratorService;
import uk.gov.companieshouse.api.strikeoffobjections.validation.ActionCodeValidator;
import uk.gov.companieshouse.api.strikeoffobjections.validation.Gaz2RequestedValidator;
import uk.gov.companieshouse.api.strikeoffobjections.validation.ValidationException;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;
import uk.gov.companieshouse.service.links.Links;

import javax.servlet.http.HttpServletResponse;
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
    private static final String ATTACHMENT_NOT_DELETED = "Unable to delete attachment %s, status code %s";
    private static final String ATTACHMENT_NOT_DELETED_SHORT = "Unable to delete attachment %s";
    private static final String INVALID_PATCH_STATUS = "Unable to patch status to %s for Objection id: %s";

    @Autowired
    private ObjectionRepository objectionRepository;

    @Autowired
    private ApiLogger logger;

    @Autowired
    private Supplier<LocalDateTime> dateTimeSupplier;

    @Autowired
    private ObjectionPatcher objectionPatcher;

    @Autowired
    private FileTransferApiClient fileTransferApiClient;

    @Autowired
    private ERICHeaderParser ericHeaderParser;

    @Autowired
    private ObjectionProcessor objectionProcessor;

    @Autowired
    private OracleQueryClient oracleQueryClient;

    @Autowired
    private ActionCodeValidator actionCodeValidator;

    @Autowired
    private IReferenceNumberGeneratorService referenceNumberGeneratorService;

    @Autowired
    private Gaz2RequestedValidator gaz2RequestedValidator;

    @Override
    public Objection createObjection(String requestId,
                                     String companyNumber,
                                     String ericUserId,
                                     String ericUserDetails,
                                     ObjectionCreate objectionCreate) throws ServiceException {

        Map<String, Object> logMap = buildLogMap(companyNumber, null, null);
        logger.infoContext(requestId, "Creating objection", logMap);

        final Long actionCode = getActionCode(companyNumber, requestId);
        final ObjectionStatus objectionStatus = getObjectionStatusForCreate(actionCode, companyNumber, requestId);
        final String refNumber = referenceNumberGeneratorService.generateReferenceNumber();
        
        Objection entity = new Objection.Builder()
                .withCompanyNumber(companyNumber)
                .withCreatedOn(dateTimeSupplier.get())
                .withCreatedBy(buildCreatedBy(ericUserId, ericUserDetails, objectionCreate))
                .withHttpRequestId(requestId)
                .withActionCode(actionCode)
                .withStatus(objectionStatus)
                .withStatusChangedOn(dateTimeSupplier.get())
                .withId(refNumber)
                .withLinks(createLinks("/company/" + companyNumber + "/strike-off-objections/" + refNumber, false))
                .build();

        try {
            return objectionRepository.insert(entity);
        } catch (DuplicateKeyException e) {
            logger.errorContext(requestId, "Reference Number already exists", e);
            throw new ServiceException("Reference Number already exists", e);
        }
    }

    private CreatedBy buildCreatedBy(String ericUserId,
                                     String ericUserDetails,
                                     ObjectionCreate objectionCreate) {
        final String userEmailAddress = ericHeaderParser.getEmailAddress(ericUserDetails);
        return new CreatedBy(ericUserId, userEmailAddress,
                objectionCreate.getFullName(), objectionCreate.canShareIdentity());
    }

    private Long getActionCode(String companyNumber, String requestId) {
        Long actionCode = oracleQueryClient.getCompanyActionCode(companyNumber);

        logger.debugContext(requestId, "Company action code is " + actionCode);

        return actionCode;
    }

    private ObjectionStatus getObjectionStatusForCreate(Long actionCode, String companyNumber, String logContext) {
        ObjectionStatus objectionStatus = ObjectionStatus.OPEN;
        try {
            validateCompanyIsEligible(actionCode, companyNumber, logContext);
        } catch (ValidationException validationException) {
            objectionStatus = validationException.getStatus();
        }
        return objectionStatus;
    }

    public ObjectionEligibility isCompanyEligible(String companyNumber, String requestId) {
        Long actionCode = getActionCode(companyNumber, requestId);

        boolean isCompanyEligible = true;
        try {
            validateCompanyIsEligible(actionCode, companyNumber, requestId);
        } catch (ValidationException validationException) {
            isCompanyEligible = false;
        }
        return new ObjectionEligibility(isCompanyEligible);
    }

    private void validateCompanyIsEligible(Long actionCode, String companyNumber, String logContext) throws ValidationException {
        try {
            actionCodeValidator.validate(actionCode, logContext);
            gaz2RequestedValidator.validate(companyNumber, actionCode, logContext);
        } catch (ValidationException validationException) {
            Map<String, Object> logMap = buildLogMap(companyNumber, null, null);
            logMap.put(LogConstants.ACTION_CODE.getValue(), actionCode);
            logMap.put(LogConstants.OBJECTION_STATUS.getValue(), validationException.getStatus());
            logger.infoContext(logContext, "Action Code validation failed", logMap);
            throw validationException;
        }
    }

    /**
     * Update the Objection data with the provided patch data
     * Triggers the processing of the Objection if status is changed
     * from OPEN to SUBMITTED
     * @param requestId the http request id
     * @param companyNumber the company number
     * @param objectionId id of the objection
     * @param objectionPatch the data to add to the Objection
     * @throws ObjectionNotFoundException if objection not found
     * @throws InvalidObjectionStatusException if status change is not allowed
     */
    @Override
    public void patchObjection(String objectionId,
                               ObjectionPatch objectionPatch,
                               String requestId,
                               String companyNumber)
            throws ObjectionNotFoundException,
                   InvalidObjectionStatusException,
                   ServiceException {
        Map<String, Object> logMap = buildLogMap(companyNumber, objectionId, null);
        logger.debugContext(requestId, "Checking for existing objection", logMap);

        Optional<Objection> existingObjectionOptional = objectionRepository.findById(objectionId);

        if (!existingObjectionOptional.isPresent()) {
            logger.infoContext(requestId, "Objection does not exist", logMap);
            throw new ObjectionNotFoundException(String.format(OBJECTION_NOT_FOUND_MESSAGE, objectionId));
        }

        Objection existingObjection = existingObjectionOptional.get();

        validatePatchStatusChange(objectionPatch, existingObjection, requestId, companyNumber);

        logger.debugContext(requestId, "Objection exists, patching", logMap);
        ObjectionStatus previousStatus = existingObjection.getStatus();
        Objection objection = objectionPatcher.patchObjection(objectionPatch, requestId, existingObjection);
        objectionRepository.save(objection);

        // if changing status to SUBMITTED from OPEN, process the objection
        if (ObjectionStatus.SUBMITTED == objectionPatch.getStatus() && ObjectionStatus.OPEN == previousStatus) {
            objectionProcessor.process(objection, requestId);
        }
    }

    private void validatePatchStatusChange(ObjectionPatch objectionPatch,
                                           Objection existingObjection,
                                           String requestId,
                                           String companyNumber) throws InvalidObjectionStatusException {
        // TODO OBJ-177 Implement a status manager that will do the throwing if status change not allowed
        if (ObjectionStatus.SUBMITTED == objectionPatch.getStatus() && ObjectionStatus.OPEN != existingObjection.getStatus()) {
            String objectionId = existingObjection.getId();

            InvalidObjectionStatusException statusException = new InvalidObjectionStatusException(
                    String.format(INVALID_PATCH_STATUS, objectionPatch.getStatus(), objectionId));

            Map<String, Object> logMap = buildLogMap(companyNumber, objectionId, null);
            logger.errorContext(requestId, statusException.getMessage(), statusException, logMap);

            throw statusException;
        }
    }

    @Override
    public Objection getObjection(String requestId, String objectionId) throws ObjectionNotFoundException {
        return objectionRepository.findById(objectionId).orElseThrow(
                () -> new ObjectionNotFoundException(String.format(OBJECTION_NOT_FOUND_MESSAGE, objectionId))
        );
    }

    @Override
    public ServiceResult<String> addAttachment(String requestId, String objectionId, MultipartFile file, String attachmentsUri)
            throws ServiceException, ObjectionNotFoundException {
        Map<String, Object> logMap = buildLogMap(null, objectionId, null);
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
        }

        Attachment attachment = createAttachment(file, attachmentId);
        Objection objection = objectionRepository.findById(objectionId).orElseThrow(
                () -> new ObjectionNotFoundException(String.format(OBJECTION_NOT_FOUND_MESSAGE, objectionId))
        );
        objection.addAttachment(attachment);

        Links links = createLinks(attachmentsUri + "/" + attachmentId, true);
        attachment.setLinks(links);

        objectionRepository.save(objection);

        return ServiceResult.accepted(attachmentId);
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

    private Links createLinks(String selfLink, boolean createDownloadLink) {
        Links links = new Links();
        links.setLink(ObjectionsLinkKeys.SELF, selfLink);
        if (createDownloadLink) {
            links.setLink(ObjectionsLinkKeys.DOWNLOAD, selfLink + "/download");
        }
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
        Map<String, Object> logMap = buildLogMap(companyNumber, objectionId, null);
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
    public void deleteAttachment(String requestId, String objectionId, String attachmentId)
            throws ObjectionNotFoundException, AttachmentNotFoundException, ServiceException {

        Objection objection = objectionRepository.findById(objectionId).orElseThrow(
                () -> new ObjectionNotFoundException(String.format(OBJECTION_NOT_FOUND_MESSAGE, objectionId))
        );

        List<Attachment> attachments = objection.getAttachments();
        Attachment attachment = attachments.parallelStream().filter(o -> attachmentId.equals(o.getId())).findFirst().orElseThrow(
                () -> new AttachmentNotFoundException(String.format(ATTACHMENT_NOT_FOUND_MESSAGE, attachmentId))
        );

        Map<String, Object> logMap = buildLogMap(null, objectionId, attachmentId);
        deleteFromS3(requestId, attachmentId, logMap);

        attachments.remove(attachment);

        objection.setAttachments(attachments);

        objectionRepository.save(objection);

    }

    private void deleteFromS3(String requestId, String attachmentId, Map<String, Object> logMap) throws ServiceException {
        String errorMessage = null;
        try {
            FileTransferApiClientResponse response = fileTransferApiClient.delete(requestId, attachmentId);

            if (response == null || response.getHttpStatus() == null) {
                errorMessage = String.format(ATTACHMENT_NOT_DELETED_SHORT, attachmentId);
            } else if (response.getHttpStatus().isError()) {
                errorMessage = String.format(ATTACHMENT_NOT_DELETED, attachmentId, response.getHttpStatus());
            }

            if (errorMessage != null) {
                logger.infoContext(requestId, errorMessage, logMap);
                throw new ServiceException(errorMessage);
            }

        } catch (HttpClientErrorException | HttpServerErrorException e) {
            String message = String.format(ATTACHMENT_NOT_DELETED, attachmentId, e.getStatusCode());
            logger.errorContext(requestId, message, e, logMap);
            throw new ServiceException(message);
        }
    }

    public FileTransferApiClientResponse downloadAttachment(String requestId,
                                                            String objectionId,
                                                            String attachmentId,
                                                            HttpServletResponse response) throws ServiceException {
        return fileTransferApiClient.download(requestId, attachmentId, response);
    }

    // TODO OBJ-141 repetitive logging in codebase, needs centralized handler that allows for different parameters.
    private Map<String, Object> buildLogMap(String companyNumber, String objectionId, String attachmentId) {
        Map<String, Object> logMap = new HashMap<>();
        if (StringUtils.isNotBlank(companyNumber)) {
            logMap.put(LogConstants.COMPANY_NUMBER.getValue(), companyNumber);
        }
        if (StringUtils.isNotBlank(objectionId)) {
            logMap.put(LogConstants.OBJECTION_ID.getValue(), objectionId);
        }
        if (StringUtils.isNotBlank(attachmentId)) {
            logMap.put(LogConstants.ATTACHMENT_ID.getValue(), attachmentId);
        }
        return logMap;
    }
}
