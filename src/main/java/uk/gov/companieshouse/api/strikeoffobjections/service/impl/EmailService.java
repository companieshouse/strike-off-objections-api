package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.email.KafkaEmailClient;
import uk.gov.companieshouse.api.strikeoffobjections.model.email.EmailContent;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.service.ICompanyProfileService;
import uk.gov.companieshouse.api.strikeoffobjections.service.IEmailService;
import uk.gov.companieshouse.service.ServiceException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class EmailService implements IEmailService {

    @Value("${EMAIL_SENDER_APP_ID}")
    private String originatingAppId;

    @Value(("${EMAIL_SUBMITTED_EXTERNAL_TEMPLATE_MESSAGE_TYPE}"))
    private String submittedCustomerEmailType;

    @Value(("${EMAIL_SUBMITTED_INTERNAL_TEMPLATE_MESSAGE_TYPE}"))
    private String submittedDissolutionTeamEmailType;

    @Value(("${EMAIL_RECIPIENTS_CARDIFF}"))
    private String emailRecipientsCardiff;

    @Value(("${EMAIL_RECIPIENTS_EDINBURGH}"))
    private String emailRecipientsEdinburgh;

    @Value(("${EMAIL_RECIPIENTS_BELFAST}"))
    private String emailRecipientsBelfast;

    private ApiLogger logger;
    private ICompanyProfileService companyProfileService;
    private KafkaEmailClient kafkaEmailClient;
    private Supplier<LocalDateTime> dateTimeSupplier;
    private ERICHeaderParser ericHeaderParser;

    @Autowired
    public EmailService(
            ApiLogger logger,
            ICompanyProfileService companyProfileService,
            KafkaEmailClient kafkaEmailClient,
            Supplier<LocalDateTime> dateTimeSupplier,
            ERICHeaderParser ericHeaderParser
    ) {
        this.logger = logger;
        this.companyProfileService = companyProfileService;
        this.kafkaEmailClient = kafkaEmailClient;
        this.dateTimeSupplier = dateTimeSupplier;
        this.ericHeaderParser = ericHeaderParser;
    }

    @Override
    public void sendObjectionSubmittedCustomerEmail (
            String requestId,
            String ericAuthorisedUser,
            String companyNumber,
            Objection objection
    ) throws ServiceException {

        CompanyProfileApi companyProfile = companyProfileService.getCompanyProfile(requestId, companyNumber);
        String companyName = companyProfile.getCompanyName();
        Map<String, Object> data = constructEmailDataMap(companyName, companyNumber, objection);

        String emailAddress = ericHeaderParser.getEmailAddress(ericAuthorisedUser);
        EmailContent emailContent = constructEmailContent(EmailType.CUSTOMER,
                requestId, emailAddress, data);

        logger.debugContext(requestId, "Calling Kafka client to send customer email");
        kafkaEmailClient.sendEmailToKafka(emailContent);
        logger.debugContext(requestId, "Successfully called Kafka client");
    }

    @Override
    public void sendObjectionSubmittedDissolutionTeamEmail (
            String requestId,
            String ericAuthorisedUser,
            String companyNumber,
            Objection objection
    ) throws ServiceException {

        CompanyProfileApi companyProfile = companyProfileService.getCompanyProfile(requestId, companyNumber);
        String companyName = companyProfile.getCompanyName();
        Map<String, Object> data = constructEmailDataMap(companyName, companyNumber, objection);

        for (String emailAddress : getDissolutionTeamRecipients(companyProfile.getJurisdiction())) {
            EmailContent emailContent = constructEmailContent(EmailType.DISSOLUTION_TEAM,
                    requestId, ericAuthorisedUser, data);

            logger.debugContext(requestId, String.format("Calling Kafka client to send dissolution team emailto %s",
                    emailAddress));
            kafkaEmailClient.sendEmailToKafka(emailContent);
            logger.debugContext(requestId, "Successfully called Kafka client");
        }

    }

    private EmailContent constructEmailContent(EmailType emailType,
                                               String requestId,
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

    private Map<String, Object> constructEmailDataMap(String companyName, String companyNumber, Objection objection) {
        Map<String, Object> data = new HashMap<>();

        data.put("company_name", companyName);
        data.put("company_number", companyNumber);
        data.put("objection_id", objection.getId());
        data.put("reason", objection.getReason());
        data.put("attachments", objection.getAttachments());
        return data;
    }

    protected String[] getDissolutionTeamRecipients(String jurisdiction) {
        switch(jurisdiction) {
            case "england":
            case "wales":
            case "england-wales":
                return emailRecipientsCardiff.split(",");
            case "scotland":
                return emailRecipientsEdinburgh.split(",");
            case "northern-ireland":
                return emailRecipientsBelfast.split(",");
            default:
                return emailRecipientsCardiff.split(",");
        }
    }
}
