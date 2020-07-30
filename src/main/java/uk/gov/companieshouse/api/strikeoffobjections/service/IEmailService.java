package uk.gov.companieshouse.api.strikeoffobjections.service;

import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.service.ServiceException;

public interface IEmailService {
    void sendObjectionSubmittedCustomerEmail(
            String requestId,
            String ericAuthorisedUser,
            String companyNumber,
            Objection objection
    ) throws ServiceException;

    void sendObjectionSubmittedDissolutionTeamEmail(
            String requestId,
            String companyNumber,
            Objection objection
    ) throws ServiceException;
}
