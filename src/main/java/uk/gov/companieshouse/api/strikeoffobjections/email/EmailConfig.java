package uk.gov.companieshouse.api.strikeoffobjections.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailConfig {

    private String emailSubject;
    private String originatingAppId;
    private String emailAttachmentDownloadUrl;
    private String submittedCustomerEmailType;
    private String submittedDissolutionTeamEmailType;
    private String emailRecipientsCardiff;
    private String emailRecipientsEdinburgh;
    private String emailRecipientsBelfast;

    public EmailConfig(
            @Value("${EMAIL_SUBJECT}") String emailSubject,
            @Value("${EMAIL_SENDER_APP_ID}") String originatingAppId,
            @Value(("${EMAIL_ATTACHMENT_DOWNLOAD_URL}")) String emailAttachmentDownloadUrl,
            @Value(("${EMAIL_SUBMITTED_EXTERNAL_TEMPLATE_MESSAGE_TYPE}")) String submittedCustomerEmailType,
            @Value(("${EMAIL_SUBMITTED_INTERNAL_TEMPLATE_MESSAGE_TYPE}")) String submittedDissolutionTeamEmailType,
            @Value(("${EMAIL_RECIPIENTS_CARDIFF}")) String emailRecipientsCardiff,
            @Value(("${EMAIL_RECIPIENTS_EDINBURGH}")) String emailRecipientsEdinburgh,
            @Value(("${EMAIL_RECIPIENTS_BELFAST}"))String emailRecipientsBelfast) {
        this.emailSubject = emailSubject;
        this.originatingAppId = originatingAppId;
        this.emailAttachmentDownloadUrl = emailAttachmentDownloadUrl;
        this.submittedCustomerEmailType = submittedCustomerEmailType;
        this.submittedDissolutionTeamEmailType = submittedDissolutionTeamEmailType;
        this.emailRecipientsCardiff = emailRecipientsCardiff;
        this.emailRecipientsEdinburgh = emailRecipientsEdinburgh;
        this.emailRecipientsBelfast = emailRecipientsBelfast;
    }

    public String getEmailSubject() {
        return emailSubject;
    }

    public String getOriginatingAppId() {
        return originatingAppId;
    }

    public String getEmailAttachmentDownloadUrl() {
        return emailAttachmentDownloadUrl;
    }

    public String getSubmittedCustomerEmailType() {
        return submittedCustomerEmailType;
    }

    public String getSubmittedDissolutionTeamEmailType() {
        return submittedDissolutionTeamEmailType;
    }

    public String getEmailRecipientsCardiff() {
        return emailRecipientsCardiff;
    }

    public String getEmailRecipientsEdinburgh() {
        return emailRecipientsEdinburgh;
    }

    public String getEmailRecipientsBelfast() {
        return emailRecipientsBelfast;
    }
}
