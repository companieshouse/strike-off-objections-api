package uk.gov.companieshouse.api.strikeoffobjections.service;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.service.ServiceException;

public interface IEmailService {
    void sendObjectionSubmittedCustomerEmail(
            String requestId,
            String ericAuthorisedUser,
            CompanyProfileApi companyProfile,
            Objection objection
    ) throws ServiceException;

    void sendObjectionSubmittedDissolutionTeamEmail(
            String requestId,
            CompanyProfileApi companyProfile,
            Objection objection
    ) throws ServiceException;
}
