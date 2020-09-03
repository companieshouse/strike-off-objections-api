package uk.gov.companieshouse.api.strikeoffobjections.service;

import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;

public interface IChipsService {

    void sendObjection(String requestId, Objection objection);
}
