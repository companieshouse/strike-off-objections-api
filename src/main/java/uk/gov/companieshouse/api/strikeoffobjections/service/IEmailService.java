package uk.gov.companieshouse.api.strikeoffobjections.service;

import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.service.ServiceException;

public interface IEmailService {

    void sendObjectionSubmittedCustomerEmail(
            Objection objection, String companyName, String requestId) throws ServiceException;

    void sendObjectionSubmittedDissolutionTeamEmail(
            String companyName, String jurisdiction, Objection objection, String requestId)
            throws ServiceException;
}
