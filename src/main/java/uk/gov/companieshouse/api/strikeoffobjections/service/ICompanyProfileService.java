package uk.gov.companieshouse.api.strikeoffobjections.service;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;

public interface ICompanyProfileService {
    CompanyProfileApi getCompanyProfile(String requestId, String companyNumber);
}
