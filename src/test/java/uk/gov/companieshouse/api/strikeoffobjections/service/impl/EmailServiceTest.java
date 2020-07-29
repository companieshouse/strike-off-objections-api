package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.email.KafkaEmailClient;
import uk.gov.companieshouse.api.strikeoffobjections.model.email.EmailContent;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
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

    private static final String REQUEST_ID = "REQUEST_ID";
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2020, 12, 10, 8, 0);
    private static final String OBJECTION_ID = "OBJECTION_ID";
    private static final String EMAIL = "demo@ch.gov.uk";
    private static final String AUTH_USER = EMAIL + "; forename=demoForename; surname=demoSurname";
    private static final String FULL_NAME = "demoForename demoSurname";
    private static final String REASON = "THIS IS A REASON";

    @Mock
    private ApiLogger apiLogger;

    @Mock
    private ICompanyProfileService companyProfileService;

    @Mock
    private KafkaEmailClient kafkaEmailClient;

    @Mock
    private Supplier<LocalDateTime> dateTimeSupplier;

    @Mock
    private ERICHeaderParser ericHeaderParser;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendObjectionSubmittedCustomerEmail() throws ServiceException {
        when(companyProfileService.getCompanyProfile(REQUEST_ID, COMPANY_NUMBER))
                .thenReturn(Utils.getMockCompanyProfile(COMPANY_NUMBER));
        when(dateTimeSupplier.get()).thenReturn(LOCAL_DATE_TIME);
        when(ericHeaderParser.getEmailAddress(AUTH_USER)).thenReturn(EMAIL);
        when(ericHeaderParser.getFullName(AUTH_USER)).thenReturn(FULL_NAME);

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
        emailService.sendObjectionSubmittedCustomerEmail(
                REQUEST_ID,
                AUTH_USER,
                COMPANY_NUMBER,
                objection
        );

        ArgumentCaptor<EmailContent> emailContentArgumentCaptor = ArgumentCaptor.forClass(EmailContent.class);
        verify(kafkaEmailClient, times(1)).sendEmailToKafka(emailContentArgumentCaptor.capture());

        EmailContent emailContent = emailContentArgumentCaptor.getValue();

        assertEquals(EMAIL, emailContent.getEmailAddress());

        Map<String, Object> data = emailContent.getData();

        assertTrue(data.containsValue(FULL_NAME));
        assertTrue(data.containsValue(COMPANY_NUMBER));
        assertTrue(data.containsValue("Company: " + COMPANY_NUMBER));
        assertTrue(data.containsValue(OBJECTION_ID));
        assertTrue(data.containsValue(attachments));

    }
}
