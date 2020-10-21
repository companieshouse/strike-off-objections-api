package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.api.strikeoffobjections.chips.ChipsClient;
import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.service.IChipsService;

@Service
public class ChipsService implements IChipsService {

    @Value("${EMAIL_ATTACHMENT_DOWNLOAD_URL_PREFIX}")
    private String attachmentDownloadUrlPrefix;

    @Value("${FEATURE_FLAG_SEND_CHIPS_CONTACT_DATA}")
    private boolean isFeatureFlagSendChipsContactDataEnabled;

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
                getAsContactDataOrNull(objection.getAttachments()),
                getAsContactDataOrNull(objection.getId()),
                getAsContactDataOrNull(objection.getCreatedBy().getFullName()),
                getAsContactDataOrNull(objection.getCreatedBy().isShareIdentity()),
                getAsContactDataOrNull(objection.getCreatedBy().getEmail()),
                getAsContactDataOrNull(objection.getReason()),
                getAsContactDataOrNull(attachmentDownloadUrlPrefix)
        );

        this.chipsClient.sendToChips(requestId, chipsRequest);
    }

    private <T> T getAsContactDataOrNull(T data) {
        if (isFeatureFlagSendChipsContactDataEnabled) {
            return data;
        } else {
            return null;
        }
    }
}
