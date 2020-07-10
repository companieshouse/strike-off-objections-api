package uk.gov.companieshouse.api.strikeoffobjections.service;

import java.util.List;

import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;

public interface IObjectionService {
    String createObjection(String requestId, String companyNumber, String ericUserId, String ericUserDetails) throws Exception;

    void patchObjection(String requestId, String companyNumber,String objectionId, ObjectionPatch objectionPatch)
            throws ObjectionNotFoundException;

    List<Attachment> getAttachments(String requestId, String companyNumber,String objectionId)
            throws ObjectionNotFoundException;

    ServiceResult<String> addAttachment(String requestId, String objectionId, MultipartFile file, String attachmentsUri) throws ServiceException, ObjectionNotFoundException;
}
