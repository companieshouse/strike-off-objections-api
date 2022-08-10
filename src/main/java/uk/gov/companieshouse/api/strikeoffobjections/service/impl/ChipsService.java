package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import uk.gov.companieshouse.api.strikeoffobjections.chips.ChipsSender;
import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.service.IChipsService;
import uk.gov.companieshouse.service.ServiceException;

@Service
public class ChipsService implements IChipsService {

    @Value("${EMAIL_ATTACHMENT_DOWNLOAD_URL_PREFIX}")
    private String attachmentDownloadUrlPrefix;

    private final ChipsSender chipsClient;

    @Autowired
    public ChipsService(@Qualifier("chips-sender") ChipsSender chipsClient) {
        this.chipsClient = chipsClient;
    }

    @Override
    public void sendObjection(String requestId, Objection objection) throws ServiceException {
        final ChipsRequest chipsRequest = new ChipsRequest
                .Builder()
                .objectionId(objection.getId())
                .companyNumber(objection.getCompanyNumber())
                .attachments(attachmentDownloadUrlPrefix, objection.getAttachments())
                .referenceNumber(objection.getId())
                .fullName(objection.getCreatedBy().getFullName())
                .shareIdentity(
                        objection.getCreatedBy().isShareIdentity())
                .customerEmail(objection.getCreatedBy().getEmail())
                .reason(objection.getReason())
                .build();

        this.chipsClient.sendToChips(requestId, chipsRequest);
    }
}
