package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.SendEmail;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.FormatUtils;
import uk.gov.companieshouse.api.strikeoffobjections.config.EmailProperties;
import uk.gov.companieshouse.api.strikeoffobjections.exception.EmailSendException;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.service.IEmailService;

@Service
public class EmailService implements IEmailService {

    private static final String SEND_EMAIL = "/send-email";
    private static final String SUBJECT = "subject";
    private static final String DATE = "date";
    private static final String OBJECTION_ID = "objection_id";
    private static final String TO = "to";
    private static final String FULL_NAME = "full_name";
    private static final String SHARE_IDENTITY = "share_identity";
    private static final String COMPANY_NAME = "company_name";
    private static final String COMPANY_NUMBER = "company_number";
    private static final String REASON = "reason";
    private static final String ATTACHMENTS = "attachments";
    private static final String ATTACHMENTS_DOWNLOAD_URL_PREFIX = "attachments_download_url_prefix";
    private static final String CUSTOMER_NUMBER_SUBSTITUTION = "{{ COMPANY_NUMBER }}";
    private static final String CUSTOMER_EMAIL = "customer_email";
    private static final String SCOTLAND = "scotland";
    private static final String NORTHERN_IRELAND = "northern-ireland";
    private static final String SUCCESSFULLY_SENT_EMAIL_LOG = "Email sent successfully via CHS-Kafka-API";
    private static final String FAILED_TO_SEND_EMAIL = "Failed to send email via CHS-Kafka-API";

    private final ObjectMapper objectMapper = new ObjectMapper();
    private final ApiLogger logger;
    private final Supplier<InternalApiClient> internalApiClient;
    private final EmailProperties emailProperties;

    @Autowired
    public EmailService(ApiLogger logger, Supplier<InternalApiClient> internalApiClient, EmailProperties emailProperties) {
        this.logger = logger;
        this.internalApiClient = internalApiClient;
        this.emailProperties = emailProperties;
    }

    @Override
    public void sendObjectionSubmittedCustomerEmail(Objection objection, String companyName, String requestId) {
        String emailAddress = objection.getCreatedBy().getEmail();
        Map<String, Object> data = constructCommonEmailMap(companyName, objection, emailAddress);
        SendEmail emailContent = constructChsKafkaApiMessage(EmailType.CUSTOMER, emailAddress, data);
        logger.debugContext(requestId, "Calling CHS-Kafka-API client to send customer email");
        sendEmailMessageToChsKafkaApi(emailContent, requestId);
    }

    @Override
    public void  sendObjectionSubmittedDissolutionTeamEmail(String companyName, String jurisdiction, Objection objection,
            String requestId) {
        for (String emailAddress : getDissolutionTeamRecipients(jurisdiction)) {
            Map<String, Object> data = constructCommonEmailMap(companyName, objection, emailAddress);
            data.put(CUSTOMER_EMAIL, objection.getCreatedBy().getEmail());
            SendEmail emailContent = constructChsKafkaApiMessage(EmailType.DISSOLUTION_TEAM, emailAddress, data);
            logger.debugContext(requestId,
                    String.format("Calling CHS-Kafka-API client to send dissolution team email to %s", emailAddress));
            sendEmailMessageToChsKafkaApi(emailContent, requestId);
        }
    }

    private SendEmail constructChsKafkaApiMessage(EmailType emailType, String emailAddress, Map<String, Object> data) {
        String typeOfEmail = (emailType == EmailType.CUSTOMER) ? emailProperties.getSubmittedExternalTemplateMessageType()
                : emailProperties.getSubmittedInternalTemplateMessageType();
        SendEmail sendEmail = new SendEmail();
        sendEmail.setAppId(emailProperties.getSenderAppId());
        sendEmail.setMessageId(UUID.randomUUID().toString());
        sendEmail.setMessageType(typeOfEmail);
        sendEmail.setEmailAddress(emailAddress);

        try {
            String jsonData = objectMapper.writeValueAsString(data);
            sendEmail.setJsonData(jsonData);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize email data to JSON", e);
        }
        return sendEmail;
    }

    private Map<String, Object> constructCommonEmailMap(String companyName, Objection objection, String email) {
        Map<String, Object> data = new HashMap<>();

        LocalDate submittedOn = objection.getCreatedOn().toLocalDate();

        String subject = emailProperties.getSubject().replace(CUSTOMER_NUMBER_SUBSTITUTION, objection.getCompanyNumber());
        data.put(SUBJECT, subject);
        data.put(DATE, FormatUtils.formatDate(submittedOn));
        data.put(OBJECTION_ID, objection.getId());
        data.put(TO, email);
        data.put(FULL_NAME, objection.getCreatedBy().getFullName());
        data.put(SHARE_IDENTITY, objection.getCreatedBy().isShareIdentity());
        data.put(COMPANY_NAME, companyName);
        data.put(COMPANY_NUMBER, objection.getCompanyNumber());
        data.put(REASON, objection.getReason());
        data.put(ATTACHMENTS, objection.getAttachments());
        data.put(ATTACHMENTS_DOWNLOAD_URL_PREFIX, emailProperties.getAttachmentDownloadUrlPrefix());

        return data;
    }

    protected String[] getDissolutionTeamRecipients(String jurisdiction) {
        return switch (jurisdiction) {
            case SCOTLAND -> splitAndStrip(emailProperties.getRecipientsEdinburgh());
            case NORTHERN_IRELAND -> splitAndStrip(emailProperties.getRecipientsBelfast());
            default -> splitAndStrip(emailProperties.getRecipientsCardiff());
        };
    }

    private String[] splitAndStrip(String commaSeparatedString) {
        return commaSeparatedString.replace(" ", "").split(",");
    }

    private void sendEmailMessageToChsKafkaApi(SendEmail sendEmail, String requestId) {
        try {
            internalApiClient.get()
                    .sendEmailHandler()
                    .postSendEmail(SEND_EMAIL, sendEmail)
                    .execute();
            logger.debugContext(requestId, SUCCESSFULLY_SENT_EMAIL_LOG);
        } catch (Exception e) {
            logger.errorContext(requestId, FAILED_TO_SEND_EMAIL, e);
            throw new EmailSendException(FAILED_TO_SEND_EMAIL, e);
        }
    }
}
