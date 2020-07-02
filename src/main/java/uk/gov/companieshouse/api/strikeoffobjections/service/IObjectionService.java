package uk.gov.companieshouse.api.strikeoffobjections.service;

import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;

public interface IObjectionService {
    String createObjection(String requestId, String companyNumber) throws Exception;
    void patchObjection(String requestId, String companyNumber,String objectionID, ObjectionPatch objectionPatch) throws ObjectionNotFoundException;
}
