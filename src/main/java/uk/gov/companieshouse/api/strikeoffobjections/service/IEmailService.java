package uk.gov.companieshouse.api.strikeoffobjections.service;

import uk.gov.companieshouse.service.ServiceException;

import java.util.List;

public interface IEmailService {
    void sendObjectionSubmittedCustomerEmail(
            String requestId,
            String ericAuthorisedUser,
            String companyNumber,
            String objectionId,
            List<String> attachmentNames
    ) throws ServiceException;
}
