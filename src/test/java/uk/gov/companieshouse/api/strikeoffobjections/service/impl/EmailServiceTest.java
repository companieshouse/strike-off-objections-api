package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.email.KafkaEmailClient;
import uk.gov.companieshouse.api.strikeoffobjections.model.email.EmailContent;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.service.ICompanyProfileService;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.service.ServiceException;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EmailServiceTest {

    private static final String EMAIL_SUBJECT = "{{ COMPANY_NUMBER }}: email sent";
    private static final String REQUEST_ID = "REQUEST_ID";
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    private static final String FORMATTED_EMAIL_SUBJECT = COMPANY_NUMBER + ": email sent";
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2020, 12, 10, 8, 0);
    private static final String OBJECTION_ID = "OBJECTION_ID";
    private static final String EMAIL = "example@test.co.uk";
    private static final String USER_ID = "32324";
    private static final String REASON = "THIS IS A REASON";

    @Mock
    private ApiLogger apiLogger;

    @Mock
    private ICompanyProfileService companyProfileService;

    @Mock
    private KafkaEmailClient kafkaEmailClient;

    @Mock
    private Supplier<LocalDateTime> dateTimeSupplier;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendObjectionSubmittedCustomerEmail() throws ServiceException {
        ReflectionTestUtils.setField(emailService, "emailSubject", EMAIL_SUBJECT);
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, REQUEST_ID))
                .thenReturn(Utils.getDummyCompanyProfile(COMPANY_NUMBER));
        when(dateTimeSupplier.get()).thenReturn(LOCAL_DATE_TIME);

        Attachment attachment1 = new Attachment();
        Attachment attachment2 = new Attachment();
        attachment1.setName("Name 1");
        attachment2.setName("Name 2");

        List<Attachment> attachments = Arrays.asList(
                attachment1, attachment2
        );

        Objection objection = new Objection();
        objection.setReason(REASON);
        objection.setId(OBJECTION_ID);
        objection.setAttachments(attachments);
        objection.setCompanyNumber(COMPANY_NUMBER);
        CreatedBy createdBy = new CreatedBy(USER_ID, EMAIL);
        objection.setCreatedBy(createdBy);

        emailService.sendObjectionSubmittedCustomerEmail(
                objection,
                REQUEST_ID
        );

        ArgumentCaptor<EmailContent> emailContentArgumentCaptor = ArgumentCaptor.forClass(EmailContent.class);
        verify(kafkaEmailClient, times(1)).sendEmailToKafka(emailContentArgumentCaptor.capture());

        EmailContent emailContent = emailContentArgumentCaptor.getValue();

        assertEquals(EMAIL, emailContent.getEmailAddress());

        Map<String, Object> data = emailContent.getData();

        assertTrue(data.containsValue(COMPANY_NUMBER));
        assertTrue(data.containsValue("Company: " + COMPANY_NUMBER));
        assertTrue(data.containsValue(OBJECTION_ID));
        assertTrue(data.containsValue(attachments));
        assertTrue(data.containsKey("to"));
        assertTrue(data.containsValue(EMAIL));
        assertTrue(data.containsKey("subject"));
        assertTrue(data.containsValue(FORMATTED_EMAIL_SUBJECT));
    }
}
