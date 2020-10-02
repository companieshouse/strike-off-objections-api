package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.api.strikeoffobjections.chips.ChipsClient;
import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.service.IChipsService;

@Service
public class ChipsService implements IChipsService {

    private final ChipsClient chipsClient;

    @Autowired
    public ChipsService(ChipsClient chipsClient) {
        this.chipsClient = chipsClient;
    }

    @Override
    public void sendObjection(String requestId, Objection objection) {
        ChipsRequest chipsRequest = new ChipsRequest(
                objection.getId(),
                objection.getCompanyNumber(),
                objection.getAttachments(),
                objection.getCreatedBy().getEmail(),
                objection.getReason()
        );

        this.chipsClient.sendToChips(requestId, chipsRequest);
    }
}
