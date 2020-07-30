package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.BeforeEach;
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
    private static final String REASON = "THIS IS A REASON";

    private static final String EMAIL_RECIPIENTS_CARDIFF_TEST =
            "COTIndividual@companieshouse.gov.uk,COTExttest@companieshouse.gov.uk,dissolution@companieshouse.gov.uk";
    private static final String EMAIL_RECIPIENTS_EDINBURGH_TEST = "edinIndividual@companieshouse.gov.uk,edintest@companieshouse.gov.uk";
    private static final String EMAIL_RECIPIENTS_BELFAST_TEST = "belfastIndividual@companieshouse.gov.uk,belfastExttest@companieshouse.gov.uk";

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
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, REQUEST_ID))
                .thenReturn(Utils.getDummyCompanyProfile(COMPANY_NUMBER, ""));
        when(dateTimeSupplier.get()).thenReturn(LOCAL_DATE_TIME);
        when(ericHeaderParser.getEmailAddress(AUTH_USER)).thenReturn(EMAIL);
        List<Attachment> attachments = Utils.getTestAttachments();
        Objection objection = Utils.getTestObjection(OBJECTION_ID, REASON, attachments);

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

        assertTrue(data.containsValue(COMPANY_NUMBER));
        assertTrue(data.containsValue("Company: " + COMPANY_NUMBER));
        assertTrue(data.containsValue(OBJECTION_ID));
        assertTrue(data.containsValue(attachments));
    }

    @Test
    void sendObjectionSubmittedDissolutonEmailsWalesJurisdiction() throws ServiceException {
        when(dateTimeSupplier.get()).thenReturn(LOCAL_DATE_TIME);
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, REQUEST_ID))
                .thenReturn(Utils.getDummyCompanyProfile(COMPANY_NUMBER, "wales"));
        when(config.getEmailRecipientsCardiff()).thenReturn(EMAIL_RECIPIENTS_CARDIFF_TEST);
        List<Attachment> attachments = Utils.getTestAttachments();
        Objection objection = Utils.getTestObjection(OBJECTION_ID, REASON, attachments);

        emailService.sendObjectionSubmittedDissolutionTeamEmail(
                REQUEST_ID,
                COMPANY_NUMBER,
                objection
        );

        ArgumentCaptor<EmailContent> emailContentArgumentCaptor = ArgumentCaptor.forClass(EmailContent.class);
        verify(kafkaEmailClient, times(3)).sendEmailToKafka(emailContentArgumentCaptor.capture());
    }

    @Test
    void sendObjectionSubmittedDissolutonEmailsScotlandJurisdiction() throws ServiceException {
        when(dateTimeSupplier.get()).thenReturn(LOCAL_DATE_TIME);
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, REQUEST_ID))
                .thenReturn(Utils.getDummyCompanyProfile(COMPANY_NUMBER, "scotland"));
        when(config.getEmailRecipientsEdinburgh()).thenReturn(EMAIL_RECIPIENTS_EDINBURGH_TEST);
        List<Attachment> attachments = Utils.getTestAttachments();
        Objection objection = Utils.getTestObjection(OBJECTION_ID, REASON, attachments);

        emailService.sendObjectionSubmittedDissolutionTeamEmail(
                REQUEST_ID,
                COMPANY_NUMBER,
                objection
        );

        ArgumentCaptor<EmailContent> emailContentArgumentCaptor = ArgumentCaptor.forClass(EmailContent.class);
        verify(kafkaEmailClient, times(2)).sendEmailToKafka(emailContentArgumentCaptor.capture());
    }

    @Test
    void sendObjectionSubmittedDissolutonEmailsNIJurisdiction() throws ServiceException {
        when(dateTimeSupplier.get()).thenReturn(LOCAL_DATE_TIME);
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, REQUEST_ID))
                .thenReturn(Utils.getDummyCompanyProfile(COMPANY_NUMBER, "northern-ireland"));
        when(config.getEmailRecipientsBelfast()).thenReturn(EMAIL_RECIPIENTS_BELFAST_TEST);
        List<Attachment> attachments = Utils.getTestAttachments();
        Objection objection = Utils.getTestObjection(OBJECTION_ID, REASON, attachments);

        emailService.sendObjectionSubmittedDissolutionTeamEmail(
                REQUEST_ID,
                COMPANY_NUMBER,
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
        assertEquals("COTIndividual@companieshouse.gov.uk",recipients[0]);
        assertEquals("COTExttest@companieshouse.gov.uk",recipients[1]);
        assertEquals("dissolution@companieshouse.gov.uk",recipients[2]);

        recipients = emailService.getDissolutionTeamRecipients("wales");
        assertEquals("COTIndividual@companieshouse.gov.uk",recipients[0]);
        assertEquals("COTExttest@companieshouse.gov.uk",recipients[1]);
        assertEquals("dissolution@companieshouse.gov.uk",recipients[2]);

        recipients = emailService.getDissolutionTeamRecipients("england-wales");
        assertEquals("COTIndividual@companieshouse.gov.uk",recipients[0]);
        assertEquals("COTExttest@companieshouse.gov.uk",recipients[1]);
        assertEquals("dissolution@companieshouse.gov.uk",recipients[2]);

        recipients = emailService.getDissolutionTeamRecipients("scotland");
        assertEquals("edinIndividual@companieshouse.gov.uk",recipients[0]);
        assertEquals("edintest@companieshouse.gov.uk",recipients[1]);

        recipients = emailService.getDissolutionTeamRecipients("northern-ireland");
        assertEquals("belfastIndividual@companieshouse.gov.uk",recipients[0]);
        assertEquals("belfastExttest@companieshouse.gov.uk",recipients[1]);

        recipients = emailService.getDissolutionTeamRecipients("united-kingdom");
        assertEquals("COTIndividual@companieshouse.gov.uk",recipients[0]);
        assertEquals("COTExttest@companieshouse.gov.uk",recipients[1]);

        recipients = emailService.getDissolutionTeamRecipients("something-else");
        assertEquals("COTIndividual@companieshouse.gov.uk",recipients[0]);
        assertEquals("COTExttest@companieshouse.gov.uk",recipients[1]);
    }
}
