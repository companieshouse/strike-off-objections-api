package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.FormatUtils;
import uk.gov.companieshouse.api.strikeoffobjections.email.KafkaEmailClient;
import uk.gov.companieshouse.api.strikeoffobjections.model.email.EmailContent;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.service.IEmailService;
import uk.gov.companieshouse.service.ServiceException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class EmailService implements IEmailService {

    private ApiLogger logger;
    private KafkaEmailClient kafkaEmailClient;
    private Supplier<LocalDateTime> dateTimeSupplier;

    @Value("${EMAIL_SUBJECT}")
    private String emailSubject;

    @Value("${EMAIL_SENDER_APP_ID}")
    private String originatingAppId;

    @Value("${EMAIL_ATTACHMENT_DOWNLOAD_URL_PREFIX}")
    private String emailAttachmentDownloadUrlPrefix;

    @Value("${EMAIL_SUBMITTED_EXTERNAL_TEMPLATE_MESSAGE_TYPE}")
    private String submittedCustomerEmailType;

    @Value("${EMAIL_SUBMITTED_INTERNAL_TEMPLATE_MESSAGE_TYPE}")
    private String submittedDissolutionTeamEmailType;

    @Value("${EMAIL_RECIPIENTS_CARDIFF}")
    private String emailRecipientsCardiff;

    @Value("${EMAIL_RECIPIENTS_EDINBURGH}")
    private String emailRecipientsEdinburgh;

    @Value("${EMAIL_RECIPIENTS_BELFAST}")
    private String emailRecipientsBelfast;

    @Autowired
    public EmailService(
            ApiLogger logger,
            KafkaEmailClient kafkaEmailClient,
            Supplier<LocalDateTime> dateTimeSupplier
    ) {
        this.logger = logger;
        this.kafkaEmailClient = kafkaEmailClient;
        this.dateTimeSupplier = dateTimeSupplier;
    }

    @Override
    public void sendObjectionSubmittedCustomerEmail(
            Objection objection,
            String companyName,
            String requestId
    ) throws ServiceException {

        String emailAddress = objection.getCreatedBy().getEmail();
        Map<String, Object> data = constructCommonEmailMap(
                companyName,
                objection,
                emailAddress);

        EmailContent emailContent = constructEmailContent(EmailType.CUSTOMER,
                emailAddress, data);

        logger.debugContext(requestId, "Calling Kafka client to send customer email");
        kafkaEmailClient.sendEmailToKafka(emailContent);
        logger.debugContext(requestId, "Successfully called Kafka client");
    }

    @Override
    public void sendObjectionSubmittedDissolutionTeamEmail(
            String companyName,
            String jurisdiction,
            Objection objection,
            String requestId
    ) throws ServiceException {


        for (String emailAddress : getDissolutionTeamRecipients(jurisdiction)) {
            Map<String, Object> data = constructCommonEmailMap(
                    companyName,
                    objection,
                    emailAddress);

            data.put("email", objection.getCreatedBy().getEmail());
            EmailContent emailContent = constructEmailContent(EmailType.DISSOLUTION_TEAM,
                    emailAddress, data);
            logger.debugContext(requestId, String.format("Calling Kafka client to send dissolution team email to %s",
                    emailAddress));
            kafkaEmailClient.sendEmailToKafka(emailContent);
            logger.debugContext(requestId, "Successfully called Kafka client");
        }
    }

    private EmailContent constructEmailContent(EmailType emailType,
                                               String emailAddress,
                                               Map<String, Object> data) {

        String typeOfEmail = (emailType == EmailType.CUSTOMER)? submittedCustomerEmailType
                : submittedDissolutionTeamEmailType;

        return new EmailContent.Builder()
                .withOriginatingAppId(originatingAppId)
                .withCreatedAt(dateTimeSupplier.get())
                .withMessageType(typeOfEmail)
                .withMessageId(UUID.randomUUID().toString())
                .withEmailAddress(emailAddress)
                .withData(data)
                .build();
    }

    private Map<String, Object> constructCommonEmailMap(String companyName, Objection objection, String email) {
        Map<String, Object> data = new HashMap<>();

        LocalDate submittedOn = objection.getCreatedOn().toLocalDate();

        String subject = emailSubject.replace("{{ COMPANY_NUMBER }}", objection.getCompanyNumber());
        data.put("subject", subject);
        data.put("date", FormatUtils.formatDate(submittedOn));
        data.put("objection_id", objection.getId());
        data.put("to", email);
        data.put("full_name", objection.getCreatedBy().getFullName());
        data.put("share_identity", objection.getCreatedBy().canShareIdentity());
        data.put("company_name", companyName);
        data.put("company_number", objection.getCompanyNumber());
        data.put("reason", objection.getReason());
        data.put("attachments", objection.getAttachments());
        data.put("attachments_download_url_prefix", emailAttachmentDownloadUrlPrefix);

        return data;
    }

    protected String[] getDissolutionTeamRecipients(String jurisdiction) {
        switch(jurisdiction) {
            case "scotland":
                return splitAndStrip(emailRecipientsEdinburgh);
            case "northern-ireland":
                return splitAndStrip(emailRecipientsBelfast);
            default:
                return splitAndStrip(emailRecipientsCardiff);
        }
    }

    private String[] splitAndStrip(String commaSeparatedString) {
        return commaSeparatedString.replace(" ", "").split(",");
    }
}
