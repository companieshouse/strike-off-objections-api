package uk.gov.companieshouse.api.strikeoffobjections.service;

import java.util.List;

import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;

public interface IObjectionService {
    String createObjection(String requestId, String companyNumber) throws Exception;

    void patchObjection(String requestId, String companyNumber,String objectionId, ObjectionPatch objectionPatch)
            throws ObjectionNotFoundException;

    List<Attachment> getAttachments(String requestId, String companyNumber,String objectionId)
            throws ObjectionNotFoundException;
}
