package uk.gov.companieshouse.api.strikeoffobjections.service;

import java.security.NoSuchAlgorithmException;

public interface IIdGeneratorService {
    String generateId() throws NoSuchAlgorithmException;
}
