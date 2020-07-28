package uk.gov.companieshouse.api.strikeoffobjections.service;

public interface IEmailService {
    void sendObjectionSubmittedCustomerEmail(String requestId, String ericAuthorisedUser, String companyNumber);
}
