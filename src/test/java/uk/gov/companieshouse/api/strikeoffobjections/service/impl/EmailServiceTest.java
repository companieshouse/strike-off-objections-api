package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.SendEmail;
import uk.gov.companieshouse.api.handler.chskafka.PrivateSendEmailHandler;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.EmailSendException;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.service.ServiceException;

class EmailServiceTest {

    @Mock
    private ApiLogger logger;

    @Mock
    private PrivateSendEmailHandler sendEmailHandler;

    @Captor
    private ArgumentCaptor<SendEmail> sendEmailCaptor;

    @InjectMocks
    private EmailService emailService;

    @Mock
    private Supplier<InternalApiClient> internalApiClient;

    private static final String SUCCESSFULLY_SENT_EMAIL_LOG = "Email sent successfully via CHS-Kafka-API";

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        emailService = new EmailService(logger, internalApiClient);

        // Set private fields via reflection
        setField(emailService, "emailSubject", "Objection for {{ COMPANY_NUMBER }}");
        setField(emailService, "originatingAppId", "testAppId");
        setField(emailService, "emailAttachmentDownloadUrlPrefix", "http://download/");
        setField(emailService, "submittedCustomerEmailType", "customerType");
        setField(emailService, "submittedDissolutionTeamEmailType", "teamType");
        setField(emailService, "emailRecipientsCardiff", "cardiff1@test.com,cardiff2@test.com");
        setField(emailService, "emailRecipientsEdinburgh", "edinburgh1@test.com,edinburgh2@test.com");
        setField(emailService, "emailRecipientsBelfast", "belfast1@test.com,belfast2@test.com");
    }

    private void setField(Object target, String field, Object value) {
        try {
            var f = target.getClass().getDeclaredField(field);
            f.setAccessible(true);
            f.set(target, value);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private Objection createObjection() {
        CreatedBy user = new CreatedBy("user123", "user@test.com", "objector", "Test User", true);
        Objection objection = new Objection();
        objection.setId("obj123");
        objection.setCompanyNumber("12345678");
        objection.setCreatedOn(LocalDateTime.of(2023, 5, 10, 12, 0));
        objection.setCreatedBy(user);
        objection.setReason("Test reason");
        objection.setAttachments(Utils.getTestAttachments());
        return objection;
    }

    @Test
    void testSendObjectionSubmittedCustomerEmail_Success() throws Exception {
        Objection objection = createObjection();
        String companyName = "Test Company";
        String requestId = "req-1";

        InternalApiClient mockApiClient = org.mockito.Mockito.mock(InternalApiClient.class);
        when(internalApiClient.get()).thenReturn(mockApiClient);
        PrivateSendEmailHandler mockPrivateSendEmailHandler = org.mockito.Mockito.mock(PrivateSendEmailHandler.class);
        when(mockApiClient.sendEmailHandler()).thenReturn(mockPrivateSendEmailHandler);

        emailService.sendObjectionSubmittedCustomerEmail(objection, companyName, requestId);

        ArgumentCaptor<SendEmail> emailContentArgumentCaptor = ArgumentCaptor.forClass(SendEmail.class);
        verify(mockPrivateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), emailContentArgumentCaptor.capture());

        SendEmail sendEmail = emailContentArgumentCaptor.getValue();
        assertEquals("testAppId", sendEmail.getAppId());
        assertEquals("customerType", sendEmail.getMessageType());
        assertEquals("user@test.com", sendEmail.getEmailAddress());
        verify(logger, atLeastOnce()).debugContext(eq(requestId), contains(SUCCESSFULLY_SENT_EMAIL_LOG));
    }

    @Test
    void testSendObjectionSubmittedCustomerEmail_Failure() throws Exception {
        Objection objection = createObjection();
        String companyName = "Test Company";
        String requestId = "req-2";

        InternalApiClient mockApiClient = org.mockito.Mockito.mock(InternalApiClient.class);
        when(internalApiClient.get()).thenReturn(mockApiClient);
        PrivateSendEmailHandler mockPrivateSendEmailHandler = org.mockito.Mockito.mock(PrivateSendEmailHandler.class);
        when(mockApiClient.sendEmailHandler()).thenReturn(mockPrivateSendEmailHandler);
        doThrow(new RuntimeException("Kafka error")).when(mockPrivateSendEmailHandler).postSendEmail(anyString(), any(SendEmail.class));

        assertThrows(EmailSendException.class, () ->
                emailService.sendObjectionSubmittedCustomerEmail(objection, companyName, requestId)
        );

        verify(logger).errorContext(eq(requestId), contains("Failed to send email"), any(Exception.class));
    }

    @Test
    void testSendObjectionSubmittedDissolutionTeamEmail_Cardiff() throws Exception {
        Objection objection = createObjection();
        String companyName = "Test Company";
        String requestId = "req-3";
        String jurisdiction = "england-wales";

        InternalApiClient mockApiClient = org.mockito.Mockito.mock(InternalApiClient.class);
        when(internalApiClient.get()).thenReturn(mockApiClient);
        PrivateSendEmailHandler mockPrivateSendEmailHandler = org.mockito.Mockito.mock(PrivateSendEmailHandler.class);
        when(mockApiClient.sendEmailHandler()).thenReturn(mockPrivateSendEmailHandler);

        emailService.sendObjectionSubmittedCustomerEmail(objection, companyName, requestId);

        ArgumentCaptor<SendEmail> emailContentArgumentCaptor = ArgumentCaptor.forClass(SendEmail.class);
        verify(mockPrivateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), emailContentArgumentCaptor.capture());

        SendEmail sendEmail = emailContentArgumentCaptor.getValue();
        assertEquals("testAppId", sendEmail.getAppId());
        assertEquals("customerType", sendEmail.getMessageType());
        assertEquals("user@test.com", sendEmail.getEmailAddress());

        emailService.sendObjectionSubmittedDissolutionTeamEmail(companyName, jurisdiction, objection, requestId);

        verify(logger, atLeastOnce()).debugContext(eq(requestId), contains(SUCCESSFULLY_SENT_EMAIL_LOG));
    }

    @Test
    void testSendObjectionSubmittedDissolutionTeamEmail_Scotland() throws Exception {
        Objection objection = createObjection();
        String companyName = "Test Company";
        String requestId = "req-4";
        String jurisdiction = "scotland";

        InternalApiClient mockApiClient = org.mockito.Mockito.mock(InternalApiClient.class);
        when(internalApiClient.get()).thenReturn(mockApiClient);
        PrivateSendEmailHandler mockPrivateSendEmailHandler = org.mockito.Mockito.mock(PrivateSendEmailHandler.class);
        when(mockApiClient.sendEmailHandler()).thenReturn(mockPrivateSendEmailHandler);

        emailService.sendObjectionSubmittedCustomerEmail(objection, companyName, requestId);


        ArgumentCaptor<SendEmail> emailContentArgumentCaptor = ArgumentCaptor.forClass(SendEmail.class);
        verify(mockPrivateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), emailContentArgumentCaptor.capture());

        SendEmail sendEmail = emailContentArgumentCaptor.getValue();
        assertEquals("testAppId", sendEmail.getAppId());
        assertEquals("customerType", sendEmail.getMessageType());
        assertEquals("user@test.com", sendEmail.getEmailAddress());
        emailService.sendObjectionSubmittedDissolutionTeamEmail(companyName, jurisdiction, objection, requestId);
    }

    @Test
    void testSendObjectionSubmittedDissolutionTeamEmail_Belfast() throws Exception {
        Objection objection = createObjection();
        String companyName = "Test Company";
        String requestId = "req-5";
        String jurisdiction = "northern-ireland";

        InternalApiClient mockApiClient = org.mockito.Mockito.mock(InternalApiClient.class);
        when(internalApiClient.get()).thenReturn(mockApiClient);
        PrivateSendEmailHandler mockPrivateSendEmailHandler = org.mockito.Mockito.mock(PrivateSendEmailHandler.class);
        when(mockApiClient.sendEmailHandler()).thenReturn(mockPrivateSendEmailHandler);
        emailService.sendObjectionSubmittedCustomerEmail(objection, companyName, requestId);
        ArgumentCaptor<SendEmail> emailContentArgumentCaptor = ArgumentCaptor.forClass(SendEmail.class);
        verify(mockPrivateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), emailContentArgumentCaptor.capture());

        SendEmail sendEmail = emailContentArgumentCaptor.getValue();
        assertEquals("testAppId", sendEmail.getAppId());
        assertEquals("customerType", sendEmail.getMessageType());
        assertEquals("user@test.com", sendEmail.getEmailAddress());

        emailService.sendObjectionSubmittedDissolutionTeamEmail(companyName, jurisdiction, objection, requestId);
    }

    @Test
    void testConstructChsKafkaApiMessage_Customer() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        SendEmail email = invokeConstructChsKafkaApiMessage(EmailType.CUSTOMER, "test@test.com", data);

        assertEquals("testAppId", email.getAppId());
        assertEquals("customerType", email.getMessageType());
        assertEquals("test@test.com", email.getEmailAddress());
        assertNotNull(email.getMessageId());
        assertTrue(email.getJsonData().contains("\"key\":\"value\""));
    }

    @Test
    void testConstructChsKafkaApiMessage_DissolutionTeam() throws Exception {
        Map<String, Object> data = new HashMap<>();
        data.put("key", "value");
        SendEmail email = invokeConstructChsKafkaApiMessage(EmailType.DISSOLUTION_TEAM, "team@test.com", data);

        assertEquals("testAppId", email.getAppId());
        assertEquals("teamType", email.getMessageType());
        assertEquals("team@test.com", email.getEmailAddress());
        assertNotNull(email.getMessageId());
        assertTrue(email.getJsonData().contains("\"key\":\"value\""));
    }

    @Test
    void testConstructCommonEmailMap() {
        Objection objection = createObjection();
        String companyName = "Test Company";
        String email = "user@test.com";

        Map<String, Object> map = invokeConstructCommonEmailMap(companyName, objection, email);

        assertEquals("Objection for 12345678", map.get("subject"));
        assertEquals("obj123", map.get("objection_id"));
        assertEquals("user@test.com", map.get("to"));
        assertEquals("Test User", map.get("full_name"));
        assertEquals(true, map.get("share_identity"));
        assertEquals("Test Company", map.get("company_name"));
        assertEquals("12345678", map.get("company_number"));
        assertEquals("Test reason", map.get("reason"));
        assertEquals(Utils.getTestAttachments(), map.get("attachments"));
        assertEquals("http://download/", map.get("attachments_download_url_prefix"));
        assertNotNull(map.get("date"));
    }

    @Test
    void testGetDissolutionTeamRecipients() {
        String[] cardiff = emailService.getDissolutionTeamRecipients("england-wales");
        assertArrayEquals(new String[]{"cardiff1@test.com", "cardiff2@test.com"}, cardiff);

        String[] edinburgh = emailService.getDissolutionTeamRecipients("scotland");
        assertArrayEquals(new String[]{"edinburgh1@test.com", "edinburgh2@test.com"}, edinburgh);

        String[] belfast = emailService.getDissolutionTeamRecipients("northern-ireland");
        assertArrayEquals(new String[]{"belfast1@test.com", "belfast2@test.com"}, belfast);
    }

    // Helper methods to invoke private methods via reflection
    private SendEmail invokeConstructChsKafkaApiMessage(EmailType type, String email, Map<String, Object> data) throws Exception {
        var m = EmailService.class.getDeclaredMethod("constructChsKafkaApiMessage", EmailType.class, String.class, Map.class);
        m.setAccessible(true);
        return (SendEmail) m.invoke(emailService, type, email, data);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> invokeConstructCommonEmailMap(String companyName, Objection objection, String email) {
        try {
            var m = EmailService.class.getDeclaredMethod("constructCommonEmailMap", String.class, Objection.class, String.class);
            m.setAccessible(true);
            return (Map<String, Object>) m.invoke(emailService, companyName, objection, email);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}