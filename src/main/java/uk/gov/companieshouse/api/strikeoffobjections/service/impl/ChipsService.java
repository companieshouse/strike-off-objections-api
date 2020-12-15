package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.api.strikeoffobjections.chips.ChipsSender;
import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.service.IChipsService;

@Service
public class ChipsService implements IChipsService {

    @Value("${EMAIL_ATTACHMENT_DOWNLOAD_URL_PREFIX}")
    private String attachmentDownloadUrlPrefix;

    private final ChipsSender chipsClient;

    @Autowired
    public ChipsService(ChipsSender chipsClient) {
        this.chipsClient = chipsClient;
    }

    @Override
    public void sendObjection(String requestId, Objection objection) {
        ChipsRequest chipsRequest = new ChipsRequest(
                objection.getId(),
                objection.getCompanyNumber(),
                objection.getAttachments(),
                objection.getId(),
                objection.getCreatedBy().getFullName(),
                objection.getCreatedBy().isShareIdentity(),
                objection.getCreatedBy().getEmail(),
                objection.getReason(),
                attachmentDownloadUrlPrefix
        );

        this.chipsClient.sendToChips(requestId, chipsRequest);
    }
}
