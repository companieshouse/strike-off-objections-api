package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.function.Supplier;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.fasterxml.jackson.databind.ObjectMapper;

import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.SendEmail;
import uk.gov.companieshouse.api.handler.chskafka.PrivateSendEmailHandler;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.config.EmailProperties;
import uk.gov.companieshouse.api.strikeoffobjections.exception.EmailSendException;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;

@Unit
@ExtendWith(MockitoExtension.class)
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

    @Mock
    private EmailProperties emailProperties;

    private static final String SUCCESSFULLY_SENT_EMAIL_LOG = "Email sent successfully via CHS-Kafka-API";
    private static final String COMPANY_NAME = "Test Company";

    @BeforeEach
    void setUp() {
        lenient().when(emailProperties.getSubject()).thenReturn("Objection for {{ COMPANY_NUMBER }}");
        lenient().when(emailProperties.getSenderAppId()).thenReturn("testAppId");
        lenient().when(emailProperties.getAttachmentDownloadUrlPrefix()).thenReturn("http://download/");
        lenient().when(emailProperties.getSubmittedExternalTemplateMessageType()).thenReturn("customerType");
        lenient().when(emailProperties.getSubmittedInternalTemplateMessageType()).thenReturn("teamType");
        lenient().when(emailProperties.getRecipientsCardiff()).thenReturn("cardiff1@test.com,cardiff2@test.com");
        lenient().when(emailProperties.getRecipientsEdinburgh()).thenReturn("edinburgh1@test.com,edinburgh2@test.com");
        lenient().when(emailProperties.getRecipientsBelfast()).thenReturn("belfast1@test.com,belfast2@test.com");

        emailService = new EmailService(logger, internalApiClient, emailProperties);
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
        String requestId = "req-1";

        InternalApiClient mockApiClient = org.mockito.Mockito.mock(InternalApiClient.class);
        when(internalApiClient.get()).thenReturn(mockApiClient);
        PrivateSendEmailHandler mockPrivateSendEmailHandler = org.mockito.Mockito.mock(PrivateSendEmailHandler.class);
        when(mockApiClient.sendEmailHandler()).thenReturn(mockPrivateSendEmailHandler);

        emailService.sendObjectionSubmittedCustomerEmail(objection, COMPANY_NAME, requestId);

        ArgumentCaptor<SendEmail> emailContentArgumentCaptor = ArgumentCaptor.forClass(SendEmail.class);
        verify(mockPrivateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), emailContentArgumentCaptor.capture());

        SendEmail sendEmail = emailContentArgumentCaptor.getValue();
        assertEquals("testAppId", sendEmail.getAppId());
        assertEquals("customerType", sendEmail.getMessageType());
        assertEquals("user@test.com", sendEmail.getEmailAddress());
        verify(logger, atLeastOnce()).debugContext(eq(requestId), contains(SUCCESSFULLY_SENT_EMAIL_LOG));
    }

    @Test
    void testSendObjectionSubmittedCustomerEmail_Failure() {
        Objection objection = createObjection();
        String requestId = "req-2";

        InternalApiClient mockApiClient = org.mockito.Mockito.mock(InternalApiClient.class);
        when(internalApiClient.get()).thenReturn(mockApiClient);
        PrivateSendEmailHandler mockPrivateSendEmailHandler = org.mockito.Mockito.mock(PrivateSendEmailHandler.class);
        when(mockApiClient.sendEmailHandler()).thenReturn(mockPrivateSendEmailHandler);
        doThrow(new RuntimeException("Kafka error")).when(mockPrivateSendEmailHandler).postSendEmail(anyString(), any(SendEmail.class));

        assertThrows(EmailSendException.class, () ->
                emailService.sendObjectionSubmittedCustomerEmail(objection, COMPANY_NAME, requestId)
        );

        verify(logger).errorContext(eq(requestId), contains("Failed to send email"), any(Exception.class));
    }

    @Test
    void testSendObjectionSubmittedDissolutionTeamEmail_Cardiff() throws Exception {
        Objection objection = createObjection();
        String requestId = "req-3";
        String jurisdiction = "england-wales";

        InternalApiClient mockApiClient = org.mockito.Mockito.mock(InternalApiClient.class);
        when(internalApiClient.get()).thenReturn(mockApiClient);
        PrivateSendEmailHandler mockPrivateSendEmailHandler = org.mockito.Mockito.mock(PrivateSendEmailHandler.class);
        when(mockApiClient.sendEmailHandler()).thenReturn(mockPrivateSendEmailHandler);

        emailService.sendObjectionSubmittedCustomerEmail(objection, COMPANY_NAME, requestId);

        ArgumentCaptor<SendEmail> emailContentArgumentCaptor = ArgumentCaptor.forClass(SendEmail.class);
        verify(mockPrivateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), emailContentArgumentCaptor.capture());

        SendEmail sendEmail = emailContentArgumentCaptor.getValue();
        assertEquals("testAppId", sendEmail.getAppId());
        assertEquals("customerType", sendEmail.getMessageType());
        assertEquals("user@test.com", sendEmail.getEmailAddress());

        emailService.sendObjectionSubmittedDissolutionTeamEmail(COMPANY_NAME, jurisdiction, objection, requestId);

        verify(logger, atLeastOnce()).debugContext(eq(requestId), contains(SUCCESSFULLY_SENT_EMAIL_LOG));
    }

    @Test
    void testSendObjectionSubmittedDissolutionTeamEmail_Scotland() throws Exception {
        Objection objection = createObjection();
        String requestId = "req-4";
        String jurisdiction = "scotland";

        InternalApiClient mockApiClient = org.mockito.Mockito.mock(InternalApiClient.class);
        when(internalApiClient.get()).thenReturn(mockApiClient);
        PrivateSendEmailHandler mockPrivateSendEmailHandler = org.mockito.Mockito.mock(PrivateSendEmailHandler.class);
        when(mockApiClient.sendEmailHandler()).thenReturn(mockPrivateSendEmailHandler);

        emailService.sendObjectionSubmittedCustomerEmail(objection, COMPANY_NAME, requestId);


        ArgumentCaptor<SendEmail> emailContentArgumentCaptor = ArgumentCaptor.forClass(SendEmail.class);
        verify(mockPrivateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), emailContentArgumentCaptor.capture());

        SendEmail sendEmail = emailContentArgumentCaptor.getValue();
        assertEquals("testAppId", sendEmail.getAppId());
        assertEquals("customerType", sendEmail.getMessageType());
        assertEquals("user@test.com", sendEmail.getEmailAddress());
        emailService.sendObjectionSubmittedDissolutionTeamEmail(COMPANY_NAME, jurisdiction, objection, requestId);
    }

    @Test
    void testSendObjectionSubmittedDissolutionTeamEmail_Belfast() throws Exception {
        Objection objection = createObjection();
        String requestId = "req-5";
        String jurisdiction = "northern-ireland";

        InternalApiClient mockApiClient = org.mockito.Mockito.mock(InternalApiClient.class);
        when(internalApiClient.get()).thenReturn(mockApiClient);
        PrivateSendEmailHandler mockPrivateSendEmailHandler = org.mockito.Mockito.mock(PrivateSendEmailHandler.class);
        when(mockApiClient.sendEmailHandler()).thenReturn(mockPrivateSendEmailHandler);
        emailService.sendObjectionSubmittedCustomerEmail(objection, COMPANY_NAME, requestId);
        ArgumentCaptor<SendEmail> emailContentArgumentCaptor = ArgumentCaptor.forClass(SendEmail.class);
        verify(mockPrivateSendEmailHandler, times(1)).postSendEmail(eq("/send-email"), emailContentArgumentCaptor.capture());

        SendEmail sendEmail = emailContentArgumentCaptor.getValue();
        assertEquals("testAppId", sendEmail.getAppId());
        assertEquals("customerType", sendEmail.getMessageType());
        assertEquals("user@test.com", sendEmail.getEmailAddress());

        emailService.sendObjectionSubmittedDissolutionTeamEmail(COMPANY_NAME, jurisdiction, objection, requestId);
    }

    @Test
    void testConstructCommonEmailMap() {
        Objection objection = createObjection();
        String email = "user@test.com";

        Map<String, Object> map = invokeConstructCommonEmailMap(COMPANY_NAME, objection, email);

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

    @Test
    void testConstructChsKafkaApiMessage_CustomerType() {
        Objection objection = createObjection();
        String email = "user@test.com";
        Map<String, Object> data = invokeConstructCommonEmailMap(COMPANY_NAME, objection, email);

        when(emailProperties.getSenderAppId()).thenReturn("testAppId");
        when(emailProperties.getSubmittedExternalTemplateMessageType()).thenReturn("customerType");

        try {
            var m = EmailService.class.getDeclaredMethod("constructChsKafkaApiMessage", EmailType.class, String.class, Map.class);
            m.setAccessible(true);
            SendEmail sendEmail = (SendEmail) m.invoke(emailService, EmailType.CUSTOMER, email, data);

            assertEquals("testAppId", sendEmail.getAppId());
            assertEquals("customerType", sendEmail.getMessageType());
            assertEquals(email, sendEmail.getEmailAddress());
            assertNotNull(sendEmail.getMessageId());
            assertNotNull(sendEmail.getJsonData());
            // Check that the jsonData contains some expected fields
            assert(sendEmail.getJsonData().contains("user@test.com"));
            assert(sendEmail.getJsonData().contains("Objection for 12345678"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testConstructChsKafkaApiMessage_DissolutionTeamType() {
        Objection objection = createObjection();
        String email = "team@test.com";
        Map<String, Object> data = invokeConstructCommonEmailMap(COMPANY_NAME, objection, email);

        when(emailProperties.getSenderAppId()).thenReturn("testAppId");
        when(emailProperties.getSubmittedInternalTemplateMessageType()).thenReturn("teamType");

        try {
            var m = EmailService.class.getDeclaredMethod("constructChsKafkaApiMessage", EmailType.class, String.class, Map.class);
            m.setAccessible(true);
            SendEmail sendEmail = (SendEmail) m.invoke(emailService, EmailType.DISSOLUTION_TEAM, email, data);

            assertEquals("testAppId", sendEmail.getAppId());
            assertEquals("teamType", sendEmail.getMessageType());
            assertEquals(email, sendEmail.getEmailAddress());
            assertNotNull(sendEmail.getMessageId());
            assertNotNull(sendEmail.getJsonData());
            assert(sendEmail.getJsonData().contains("team@test.com"));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testConstructChsKafkaApiMessage_ThrowsOnJsonProcessingException() {
        Objection objection = createObjection();
        String email = "user@test.com";
        Map<String, Object> data = invokeConstructCommonEmailMap(COMPANY_NAME, objection, email);

        // Use a spy to mock the ObjectMapper to throw exception
        EmailService spyService = org.mockito.Mockito.spy(emailService);
        try {
            var objectMapperField = EmailService.class.getDeclaredField("objectMapper");
            objectMapperField.setAccessible(true);
            ObjectMapper mockMapper = org.mockito.Mockito.mock(ObjectMapper.class);
            org.mockito.Mockito.doThrow(new com.fasterxml.jackson.core.JsonProcessingException("fail"){}).when(mockMapper).writeValueAsString(any());
            objectMapperField.set(spyService, mockMapper);

            var m = EmailService.class.getDeclaredMethod("constructChsKafkaApiMessage", EmailType.class, String.class, Map.class);
            m.setAccessible(true);

            assertThrows(java.lang.reflect.InvocationTargetException.class, () -> m.invoke(spyService, EmailType.CUSTOMER, email, data));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testSplitAndStrip_RemovesSpacesAndSplits() {
        try {
            var m = EmailService.class.getDeclaredMethod("splitAndStrip", String.class);
            m.setAccessible(true);
            String input = "a@test.com, b@test.com ,c@test.com";
            String[] result = (String[]) m.invoke(emailService, input);
            assertArrayEquals(new String[]{"a@test.com", "b@test.com", "c@test.com"}, result);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
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