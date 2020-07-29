package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.email.KafkaEmailClient;
import uk.gov.companieshouse.api.strikeoffobjections.model.email.EmailContent;
import uk.gov.companieshouse.api.strikeoffobjections.service.ICompanyProfileService;
import uk.gov.companieshouse.api.strikeoffobjections.service.IEmailService;
import uk.gov.companieshouse.service.ServiceException;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class EmailService implements IEmailService {

    @Value("${EMAIL_SUBMITTED_EXTERNAL_TEMPLATE_APP_ID}")
    private String originatingAppId;

    @Value(("${EMAIL_SUBMITTED_EXTERNAL_TEMPLATE_MESSAGE_TYPE}"))
    private String submittedCustomerEmailType;

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
            String objectionId,
            List<String> attachmentNames

    ) throws ServiceException {
        CompanyProfileApi companyProfile = companyProfileService.getCompanyProfile(requestId, companyNumber);

        String companyName = companyProfile.getCompanyName();
        String fullName = ericHeaderParser.getFullName(ericAuthorisedUser);
        String emailAddress = ericHeaderParser.getEmailAddress(ericAuthorisedUser);
        Map<String, Object> data = new HashMap<>();

        data.put("full_name", fullName);
        data.put("company_name", companyName);
        data.put("company_number", companyNumber);
        data.put("objection_id", objectionId);
        data.put("attachment_names", attachmentNames);

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
