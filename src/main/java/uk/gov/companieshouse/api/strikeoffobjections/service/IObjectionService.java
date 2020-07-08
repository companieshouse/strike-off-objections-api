package uk.gov.companieshouse.api.strikeoffobjections.service;

import org.springframework.web.multipart.MultipartFile;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.ServiceResult;

public interface IObjectionService {
    String createObjection(String requestId, String companyNumber) throws Exception;
    void patchObjection(String requestId, String companyNumber,String objectionID, ObjectionPatch objectionPatch) throws ObjectionNotFoundException;
    ServiceResult<String> addAttachment(String requestId, MultipartFile file) throws ServiceException;
}
