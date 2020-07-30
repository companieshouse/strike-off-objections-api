package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.email.EmailConfig;
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

    private EmailConfig emailConfig;
    private ApiLogger logger;
    private ICompanyProfileService companyProfileService;
    private KafkaEmailClient kafkaEmailClient;
    private Supplier<LocalDateTime> dateTimeSupplier;
    private ERICHeaderParser ericHeaderParser;

    @Autowired
    public EmailService(
            EmailConfig emailConfig,
            ApiLogger logger,
            ICompanyProfileService companyProfileService,
            KafkaEmailClient kafkaEmailClient,
            Supplier<LocalDateTime> dateTimeSupplier,
            ERICHeaderParser ericHeaderParser
    ) {
        this.emailConfig = emailConfig;
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

        CompanyProfileApi companyProfile = companyProfileService.getCompanyProfile(companyNumber, requestId);

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
    public void sendObjectionSubmittedDissolutionTeamEmail(
            String requestId,
            String companyNumber,
            Objection objection
    ) throws ServiceException {

        CompanyProfileApi companyProfile = companyProfileService.getCompanyProfile(companyNumber, requestId);
        String companyName = companyProfile.getCompanyName();
        Map<String, Object> data = constructEmailDataMap(companyName, companyNumber, objection);

        for (String emailAddress : getDissolutionTeamRecipients(companyProfile.getJurisdiction())) {
            EmailContent emailContent = constructEmailContent(EmailType.DISSOLUTION_TEAM,
                    requestId, emailAddress, data);
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

        String typeOfEmail = (emailType == EmailType.CUSTOMER)? emailConfig.getSubmittedCustomerEmailType()
                : emailConfig.getSubmittedDissolutionTeamEmailType();

        return new EmailContent.Builder()
                .withOriginatingAppId(emailConfig.getOriginatingAppId())
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
                return emailConfig.getEmailRecipientsCardiff().split(",");
            case "scotland":
                return emailConfig.getEmailRecipientsEdinburgh().split(",");
            case "northern-ireland":
                return emailConfig.getEmailRecipientsBelfast().split(",");
            default:
                return emailConfig.getEmailRecipientsCardiff().split(",");
        }
    }
}
