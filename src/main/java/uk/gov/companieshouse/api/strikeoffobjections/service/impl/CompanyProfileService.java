package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.strikeoffobjections.service.ICompanyProfileService;

@Service
public class CompanyProfileService implements ICompanyProfileService {
    @Override
    public CompanyProfileApi getCompanyProfile(String requestId, String companyNumber) {

        // TODO: OBJ-175 Implement CompanyProfile getCall

        CompanyProfileApi companyProfileApi = new CompanyProfileApi();
        companyProfileApi.setCompanyNumber(companyNumber);
        companyProfileApi.setCompanyName(companyNumber);

        return companyProfileApi;
    }
}
