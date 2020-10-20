package uk.gov.companieshouse.api.strikeoffobjections.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.strikeoffobjections.exception.AttachmentNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.exception.InvalidObjectionStatusException;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.file.FileTransferApiClientResponse;
import uk.gov.companieshouse.api.strikeoffobjections.model.create.ObjectionCreate;
import uk.gov.companieshouse.api.strikeoffobjections.model.eligibility.ObjectionEligibility;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;

import javax.servlet.http.HttpServletResponse;

public interface IObjectionService {
    Objection createObjection(String requestId,
                              String companyNumber,
                              String ericUserId,
                              String ericUserDetails,
                              ObjectionCreate objectionCreate) throws ServiceException;

    void patchObjection(String objectionId, ObjectionPatch objectionPatch, String requestId, String companyNumber)
            throws ObjectionNotFoundException, InvalidObjectionStatusException, ServiceException;

    Objection getObjection(String requestId, String objectionId)
            throws ObjectionNotFoundException;

    List<Attachment> getAttachments(String requestId, String companyNumber,String objectionId)
            throws ObjectionNotFoundException;

    Attachment getAttachment(String requestId, String companyNumber, String objectionId, String attachmentId)
            throws ObjectionNotFoundException, AttachmentNotFoundException;

    ServiceResult<String> addAttachment(String requestId, String objectionId, MultipartFile file, String attachmentsUri)
            throws ServiceException, ObjectionNotFoundException;

    void deleteAttachment(String requestId, String objectionId, String attachmentId)
            throws ObjectionNotFoundException, AttachmentNotFoundException, ServiceException;

    FileTransferApiClientResponse downloadAttachment(
            String requestId, String objectionId, String attachmentId, HttpServletResponse response) throws ServiceException;

    ObjectionEligibility isCompanyEligible(String companyNumber, String requestId);
}
