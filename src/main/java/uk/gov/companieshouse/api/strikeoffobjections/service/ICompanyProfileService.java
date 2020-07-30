package uk.gov.companieshouse.api.strikeoffobjections.service;

import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.service.ServiceException;

public interface ICompanyProfileService {
    CompanyProfileApi getCompanyProfile(String companyNumber, String requestId) throws ServiceException;
}
