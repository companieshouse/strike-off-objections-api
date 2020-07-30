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

    @Value("${EMAIL_SUBJECT}")
    private String emailSubject;

    @Value("${EMAIL_SENDER_APP_ID}")
    private String originatingAppId;

    @Value(("${EMAIL_SUBMITTED_EXTERNAL_TEMPLATE_MESSAGE_TYPE}"))
    private String submittedCustomerEmailType;

    private ApiLogger logger;
    private ICompanyProfileService companyProfileService;
    private KafkaEmailClient kafkaEmailClient;
    private Supplier<LocalDateTime> dateTimeSupplier;

    @Autowired
    public EmailService(
            ApiLogger logger,
            ICompanyProfileService companyProfileService,
            KafkaEmailClient kafkaEmailClient,
            Supplier<LocalDateTime> dateTimeSupplier
    ) {
        this.logger = logger;
        this.companyProfileService = companyProfileService;
        this.kafkaEmailClient = kafkaEmailClient;
        this.dateTimeSupplier = dateTimeSupplier;
    }

    @Override
    public void sendObjectionSubmittedCustomerEmail(
            Objection objection,
            String requestId
    ) throws ServiceException {
        String companyNumber = objection.getCompanyNumber();

        CompanyProfileApi companyProfile = companyProfileService.getCompanyProfile(companyNumber, requestId);

        String companyName = companyProfile.getCompanyName();
        String emailAddress = objection.getCreatedBy().getEmail();
        Map<String, Object> data = new HashMap<>();

        String subject = emailSubject.replace("{{ COMPANY_NUMBER }}", companyNumber);
        data.put("subject", subject);
        data.put("to", emailAddress);
        data.put("company_name", companyName);
        data.put("company_number", companyNumber);
        data.put("objection_id", objection.getId());
        data.put("reason", objection.getReason());
        data.put("attachments", objection.getAttachments());

        EmailContent emailContent = new EmailContent.Builder()
                .withOriginatingAppId(originatingAppId)
                .withCreatedAt(dateTimeSupplier.get())
                .withMessageType(submittedCustomerEmailType)
                .withMessageId(UUID.randomUUID().toString())
                .withEmailAddress(emailAddress)
                .withData(data)
                .build();

        logger.debugContext(requestId, "Calling Kafka client to send email");
        kafkaEmailClient.sendEmailToKafka(emailContent);
        logger.debugContext(requestId, "Successfully called Kafka client");
    }
}
