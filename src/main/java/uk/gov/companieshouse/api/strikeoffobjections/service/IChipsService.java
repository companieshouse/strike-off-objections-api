package uk.gov.companieshouse.api.strikeoffobjections.service;

import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.service.ServiceException;

public interface IChipsService {

    void sendObjection(String requestId, Objection objection) throws ServiceException;
}
