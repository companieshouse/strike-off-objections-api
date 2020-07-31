package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.email.EmailConfig;
import uk.gov.companieshouse.api.strikeoffobjections.email.KafkaEmailClient;
import uk.gov.companieshouse.api.strikeoffobjections.model.email.EmailContent;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.service.ICompanyProfileService;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.service.ServiceException;

import java.time.LocalDateTime;
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
    private static final String COMPANY_NAME = "Company: " + COMPANY_NUMBER;
    private static final String FORMATTED_EMAIL_SUBJECT = COMPANY_NUMBER + ": email sent";
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2020, 12, 10, 8, 0);
    private static final String OBJECTION_ID = "OBJECTION_ID";
    private static final String EMAIL = "demo@ch.gov.uk";
    private static final String USER_ID = "32324";
    private static final String REASON = "THIS IS A REASON";

    private static final String EMAIL_RECIPIENTS_CARDIFF_TEST = "test1@cardiff.gov.uk,test2@cardiff.gov.uk,test3@cardiff.gov.uk";
    private static final String EMAIL_RECIPIENTS_EDINBURGH_TEST = "test1@edinburgh.gov.uk,test2@edinburgh.gov.uk";
    private static final String EMAIL_RECIPIENTS_BELFAST_TEST = "test1@belfast.gov.uk,test2@belfast.gov.uk";

    @Mock
    private EmailConfig config;

    @Mock
    private ApiLogger apiLogger;

    @Mock
    private KafkaEmailClient kafkaEmailClient;

    @Mock
    private Supplier<LocalDateTime> dateTimeSupplier;

    @InjectMocks
    private EmailService emailService;

    @Test
    void sendObjectionSubmittedCustomerEmail() throws ServiceException {
        when(config.getEmailSubject()).thenReturn(FORMATTED_EMAIL_SUBJECT);
        when(config.getEmailSubject()).thenReturn(FORMATTED_EMAIL_SUBJECT);
        when(dateTimeSupplier.get()).thenReturn(LOCAL_DATE_TIME);

        Objection objection = Utils.getTestObjection(OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL);
        List<Attachment> attachments = Utils.getTestAttachments();
        attachments.forEach(objection::addAttachment);


        emailService.sendObjectionSubmittedCustomerEmail(
                objection,
                COMPANY_NAME,
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

    @Test
    void sendObjectionSubmittedDissolutionEmailsWalesJurisdiction() throws ServiceException {
        when(dateTimeSupplier.get()).thenReturn(LOCAL_DATE_TIME);
        when(config.getEmailSubject()).thenReturn(FORMATTED_EMAIL_SUBJECT);
        when(config.getEmailRecipientsCardiff()).thenReturn(EMAIL_RECIPIENTS_CARDIFF_TEST);
        Objection objection = Utils.getTestObjection(OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL);
        Utils.getTestAttachments().forEach(objection::addAttachment);

        emailService.sendObjectionSubmittedDissolutionTeamEmail(
                Utils.getDummyCompanyProfile(COMPANY_NUMBER, "wales"),
                objection,
                REQUEST_ID
        );

        ArgumentCaptor<EmailContent> emailContentArgumentCaptor = ArgumentCaptor.forClass(EmailContent.class);
        verify(kafkaEmailClient, times(3)).sendEmailToKafka(emailContentArgumentCaptor.capture());

        List<EmailContent> emailContentList = emailContentArgumentCaptor.getAllValues();
        assertEquals("test1@cardiff.gov.uk", emailContentList.get(0).getEmailAddress());
        assertEquals("test2@cardiff.gov.uk", emailContentList.get(1).getEmailAddress());
        assertEquals("test3@cardiff.gov.uk", emailContentList.get(2).getEmailAddress());
    }

    @Test
    void sendObjectionSubmittedDissolutionEmailsScotlandJurisdiction() throws ServiceException {
        when(dateTimeSupplier.get()).thenReturn(LOCAL_DATE_TIME);
        when(config.getEmailSubject()).thenReturn(FORMATTED_EMAIL_SUBJECT);
        when(config.getEmailRecipientsEdinburgh()).thenReturn(EMAIL_RECIPIENTS_EDINBURGH_TEST);
        Objection objection = Utils.getTestObjection(OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL);
        Utils.getTestAttachments().forEach(objection::addAttachment);

        emailService.sendObjectionSubmittedDissolutionTeamEmail(
                Utils.getDummyCompanyProfile(COMPANY_NUMBER, "scotland"),
                objection,
                REQUEST_ID
        );

        ArgumentCaptor<EmailContent> emailContentArgumentCaptor = ArgumentCaptor.forClass(EmailContent.class);
        verify(kafkaEmailClient, times(2)).sendEmailToKafka(emailContentArgumentCaptor.capture());

        List<EmailContent> emailContentList = emailContentArgumentCaptor.getAllValues();
        assertEquals("test1@edinburgh.gov.uk", emailContentList.get(0).getEmailAddress());
        assertEquals("test2@edinburgh.gov.uk", emailContentList.get(1).getEmailAddress());
    }

    @Test
    void sendObjectionSubmittedDissolutionEmailsNIJurisdiction() throws ServiceException {
        when(dateTimeSupplier.get()).thenReturn(LOCAL_DATE_TIME);
        when(config.getEmailSubject()).thenReturn(FORMATTED_EMAIL_SUBJECT);
        when(config.getEmailRecipientsBelfast()).thenReturn(EMAIL_RECIPIENTS_BELFAST_TEST);
        Objection objection = Utils.getTestObjection(OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL);
        Utils.getTestAttachments().forEach(objection::addAttachment);

        emailService.sendObjectionSubmittedDissolutionTeamEmail(
                Utils.getDummyCompanyProfile(COMPANY_NUMBER, "northern-ireland"),
                objection,
                REQUEST_ID
        );

        ArgumentCaptor<EmailContent> emailContentArgumentCaptor = ArgumentCaptor.forClass(EmailContent.class);
        verify(kafkaEmailClient, times(2)).sendEmailToKafka(emailContentArgumentCaptor.capture());

        List<EmailContent> emailContentList = emailContentArgumentCaptor.getAllValues();
        assertEquals("test1@belfast.gov.uk", emailContentList.get(0).getEmailAddress());
        assertEquals("test2@belfast.gov.uk", emailContentList.get(1).getEmailAddress());
    }

    @Test
    public void testRegionalEmailAddresses() {
        when(config.getEmailRecipientsCardiff()).thenReturn(EMAIL_RECIPIENTS_CARDIFF_TEST);
        when(config.getEmailRecipientsEdinburgh()).thenReturn(EMAIL_RECIPIENTS_EDINBURGH_TEST);
        when(config.getEmailRecipientsBelfast()).thenReturn(EMAIL_RECIPIENTS_BELFAST_TEST);
        String[] recipients;
        recipients = emailService.getDissolutionTeamRecipients("england");
        assertEquals("test1@cardiff.gov.uk", recipients[0]);
        assertEquals("test2@cardiff.gov.uk", recipients[1]);
        assertEquals("test3@cardiff.gov.uk", recipients[2]);

        recipients = emailService.getDissolutionTeamRecipients("wales");
        assertEquals("test1@cardiff.gov.uk", recipients[0]);
        assertEquals("test2@cardiff.gov.uk", recipients[1]);
        assertEquals("test3@cardiff.gov.uk", recipients[2]);

        recipients = emailService.getDissolutionTeamRecipients("england-wales");
        assertEquals("test1@cardiff.gov.uk", recipients[0]);
        assertEquals("test2@cardiff.gov.uk", recipients[1]);
        assertEquals("test3@cardiff.gov.uk", recipients[2]);

        recipients = emailService.getDissolutionTeamRecipients("scotland");
        assertEquals("test1@edinburgh.gov.uk", recipients[0]);
        assertEquals("test2@edinburgh.gov.uk", recipients[1]);

        recipients = emailService.getDissolutionTeamRecipients("northern-ireland");
        assertEquals("test1@belfast.gov.uk", recipients[0]);
        assertEquals("test2@belfast.gov.uk", recipients[1]);

        recipients = emailService.getDissolutionTeamRecipients("united-kingdom");
        assertEquals("test1@cardiff.gov.uk", recipients[0]);
        assertEquals("test2@cardiff.gov.uk", recipients[1]);
        assertEquals("test3@cardiff.gov.uk", recipients[2]);

        recipients = emailService.getDissolutionTeamRecipients("something-else");
        assertEquals("test1@cardiff.gov.uk", recipients[0]);
        assertEquals("test2@cardiff.gov.uk", recipients[1]);
        assertEquals("test3@cardiff.gov.uk", recipients[2]);
    }
}
