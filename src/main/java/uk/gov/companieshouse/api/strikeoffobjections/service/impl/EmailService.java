package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.service.ICompanyProfileService;
import uk.gov.companieshouse.api.strikeoffobjections.service.IEmailService;

@Service
public class EmailService implements IEmailService {

    private ApiLogger logger;
    private ICompanyProfileService companyProfileService;

    @Autowired
    public EmailService(ApiLogger logger, ICompanyProfileService companyProfileService) {
        this.logger = logger;
        this.companyProfileService = companyProfileService;
    }

    @Override
    public void sendObjectionSubmittedCustomerEmail(String requestId, String ericAuthorisedUser, String companyNumber) {
        CompanyProfileApi companyProfile = companyProfileService.getCompanyProfile(requestId, companyNumber);

    }
}
