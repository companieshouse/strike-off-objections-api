package uk.gov.companieshouse.api.strikeoffobjections.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "email")
public class EmailProperties {
    private String subject;
    private String senderAppId;
    private String attachmentDownloadUrlPrefix;
    private String submittedExternalTemplateMessageType;
    private String submittedInternalTemplateMessageType;
    private String recipientsCardiff;
    private String recipientsEdinburgh;
    private String recipientsBelfast;

    // Getters and setters
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }

    public String getSenderAppId() { return senderAppId; }
    public void setSenderAppId(String senderAppId) { this.senderAppId = senderAppId; }

    public String getAttachmentDownloadUrlPrefix() { return attachmentDownloadUrlPrefix; }
    public void setAttachmentDownloadUrlPrefix(String attachmentDownloadUrlPrefix) { this.attachmentDownloadUrlPrefix = attachmentDownloadUrlPrefix; }

    public String getSubmittedExternalTemplateMessageType() { return submittedExternalTemplateMessageType; }
    public void setSubmittedExternalTemplateMessageType(String submittedExternalTemplateMessageType) { this.submittedExternalTemplateMessageType = submittedExternalTemplateMessageType; }

    public String getSubmittedInternalTemplateMessageType() { return submittedInternalTemplateMessageType; }
    public void setSubmittedInternalTemplateMessageType(String submittedInternalTemplateMessageType) { this.submittedInternalTemplateMessageType = submittedInternalTemplateMessageType; }

    public String getRecipientsCardiff() { return recipientsCardiff; }
    public void setRecipientsCardiff(String recipientsCardiff) { this.recipientsCardiff = recipientsCardiff; }

    public String getRecipientsEdinburgh() { return recipientsEdinburgh; }
    public void setRecipientsEdinburgh(String recipientsEdinburgh) { this.recipientsEdinburgh = recipientsEdinburgh; }

    public String getRecipientsBelfast() { return recipientsBelfast; }
    public void setRecipientsBelfast(String recipientsBelfast) { this.recipientsBelfast = recipientsBelfast; }
}
