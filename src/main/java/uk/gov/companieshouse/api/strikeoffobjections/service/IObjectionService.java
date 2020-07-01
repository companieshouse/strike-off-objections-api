package uk.gov.companieshouse.api.strikeoffobjections.service;

import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.model.request.ObjectionRequest;

public interface IObjectionService {
    String createObjection(String requestId, String companyNumber) throws Exception;
    void patchObjection(String requestId, String companyNumber,String objectionID, ObjectionRequest objectionRequest) throws ObjectionNotFoundException;
}
