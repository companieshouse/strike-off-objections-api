package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.email.EmailConfig;
import uk.gov.companieshouse.api.strikeoffobjections.email.KafkaEmailClient;
import uk.gov.companieshouse.api.strikeoffobjections.model.email.EmailContent;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
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

    private static final String REQUEST_ID = "REQUEST_ID";
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2020, 12, 10, 8, 0);
    private static final String OBJECTION_ID = "OBJECTION_ID";
    private static final String EMAIL = "demo@ch.gov.uk";
    private static final String AUTH_USER = EMAIL + "; forename=demoForename; surname=demoSurname";

    private static final String EMAIL_RECIPIENTS_CARDIFF_TEST = "test1@cardiff.gov.uk,test2@cardiff.gov.uk,test3@cardiff.gov.uk";
    private static final String EMAIL_RECIPIENTS_EDINBURGH_TEST = "test1@endinburgh.gov.uk,test2@endinburgh.gov.uk";
    private static final String EMAIL_RECIPIENTS_BELFAST_TEST = "test1@belfast.gov.uk,test2@belfast.gov.uk";

    @Mock
    private EmailConfig config;

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
        when(dateTimeSupplier.get()).thenReturn(LOCAL_DATE_TIME);
        when(ericHeaderParser.getEmailAddress(AUTH_USER)).thenReturn(EMAIL);

        Objection objection = Utils.getTestObjection(OBJECTION_ID);
        List<Attachment> attachments = Utils.getTestAttachments();
        attachments.forEach(objection::addAttachment);

        emailService.sendObjectionSubmittedCustomerEmail(
                REQUEST_ID,
                AUTH_USER,
                Utils.getDummyCompanyProfile(COMPANY_NUMBER, "wales"),
                objection
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
    }

    @Test
    void sendObjectionSubmittedDissolutionEmailsWalesJurisdiction() throws ServiceException {
        when(dateTimeSupplier.get()).thenReturn(LOCAL_DATE_TIME);
        when(config.getEmailRecipientsCardiff()).thenReturn(EMAIL_RECIPIENTS_CARDIFF_TEST);
        Objection objection = Utils.getTestObjection(OBJECTION_ID);
        Utils.getTestAttachments().forEach(objection::addAttachment);

        emailService.sendObjectionSubmittedDissolutionTeamEmail(
                REQUEST_ID,
                Utils.getDummyCompanyProfile(COMPANY_NUMBER, "wales"),
                objection
        );

        ArgumentCaptor<EmailContent> emailContentArgumentCaptor = ArgumentCaptor.forClass(EmailContent.class);
        verify(kafkaEmailClient, times(3)).sendEmailToKafka(emailContentArgumentCaptor.capture());
    }

    @Test
    void sendObjectionSubmittedDissolutionEmailsScotlandJurisdiction() throws ServiceException {
        when(dateTimeSupplier.get()).thenReturn(LOCAL_DATE_TIME);
        when(config.getEmailRecipientsEdinburgh()).thenReturn(EMAIL_RECIPIENTS_EDINBURGH_TEST);
        Objection objection = Utils.getTestObjection(OBJECTION_ID);
        Utils.getTestAttachments().forEach(objection::addAttachment);

        emailService.sendObjectionSubmittedDissolutionTeamEmail(
                REQUEST_ID,
                Utils.getDummyCompanyProfile(COMPANY_NUMBER, "scotland"),
                objection
        );

        ArgumentCaptor<EmailContent> emailContentArgumentCaptor = ArgumentCaptor.forClass(EmailContent.class);
        verify(kafkaEmailClient, times(2)).sendEmailToKafka(emailContentArgumentCaptor.capture());
    }

    @Test
    void sendObjectionSubmittedDissolutionEmailsNIJurisdiction() throws ServiceException {
        when(dateTimeSupplier.get()).thenReturn(LOCAL_DATE_TIME);
        when(config.getEmailRecipientsBelfast()).thenReturn(EMAIL_RECIPIENTS_BELFAST_TEST);
        Objection objection = Utils.getTestObjection(OBJECTION_ID);
        Utils.getTestAttachments().forEach(objection::addAttachment);

        emailService.sendObjectionSubmittedDissolutionTeamEmail(
                REQUEST_ID,
                Utils.getDummyCompanyProfile(COMPANY_NUMBER, "northern-ireland"),
                objection
        );

        ArgumentCaptor<EmailContent> emailContentArgumentCaptor = ArgumentCaptor.forClass(EmailContent.class);
        verify(kafkaEmailClient, times(2)).sendEmailToKafka(emailContentArgumentCaptor.capture());
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
        assertEquals("test1@endinburgh.gov.uk", recipients[0]);
        assertEquals("test2@endinburgh.gov.uk", recipients[1]);

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
