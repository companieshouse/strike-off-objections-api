package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.InternalApiClient;
import uk.gov.companieshouse.api.chskafka.SendEmail;
import uk.gov.companieshouse.api.error.ApiErrorResponseException;
import uk.gov.companieshouse.api.handler.chskafka.PrivateSendEmailHandler;
import uk.gov.companieshouse.api.handler.chskafka.request.PrivateSendEmailPost;
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

    private static final String COMPANY_NAME = "Test Company";

    private EmailService emailService;
    @Mock
    private ApiLogger logger;
    @Mock
    private InternalApiClient internalApiClient;
    @Mock
    private EmailProperties emailProperties;

    @Mock
    private PrivateSendEmailHandler privateSendEmailHandler;
    @Mock
    private PrivateSendEmailPost privateSendEmailPost;
    @Captor
    private ArgumentCaptor<SendEmail> sendEmailCaptor;

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

        emailService = new EmailService(logger, () -> internalApiClient, emailProperties);
    }

    @Test
    void testSendObjectionSubmittedCustomerEmailSuccess() throws Exception {
        Objection objection = createObjection();
        String requestId = "req-1";

        when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);
        when(privateSendEmailHandler.postSendEmail(anyString(), any())).thenReturn(privateSendEmailPost);

        emailService.sendObjectionSubmittedCustomerEmail(objection, COMPANY_NAME, requestId);

        verify(internalApiClient).sendEmailHandler();
        verify(privateSendEmailHandler).postSendEmail(eq("/send-email"), sendEmailCaptor.capture());
        verify(privateSendEmailPost).execute();

        SendEmail sendEmail = sendEmailCaptor.getValue();
        assertEquals("testAppId", sendEmail.getAppId());
        assertEquals("customerType", sendEmail.getMessageType());
        assertEquals("user@test.com", sendEmail.getEmailAddress());
    }

    @Test
    void testSendObjectionSubmittedCustomerEmailFailure() throws ApiErrorResponseException {
        Objection objection = createObjection();
        String requestId = "req-2";

        when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);
        when(privateSendEmailHandler.postSendEmail(anyString(), any())).thenReturn(privateSendEmailPost);
        when(privateSendEmailPost.execute()).thenThrow(new RuntimeException("Kafka error"));

        assertThrows(EmailSendException.class,
                () -> emailService.sendObjectionSubmittedCustomerEmail(objection, COMPANY_NAME, requestId));
        verify(internalApiClient).sendEmailHandler();
        verify(privateSendEmailHandler).postSendEmail(eq("/send-email"), any());
        verify(privateSendEmailPost).execute();
    }

    @Test
    void testSendObjectionSubmittedDissolutionTeamEmail() throws Exception {
        Objection objection = createObjection();
        String requestId = "req-3";
        String jurisdiction = "england-wales";

        when(internalApiClient.sendEmailHandler()).thenReturn(privateSendEmailHandler);
        when(privateSendEmailHandler.postSendEmail(anyString(), any())).thenReturn(privateSendEmailPost);

        emailService.sendObjectionSubmittedDissolutionTeamEmail(COMPANY_NAME, jurisdiction, objection, requestId);

        verify(internalApiClient, times(2)).sendEmailHandler();
        verify(privateSendEmailHandler, times(2)).postSendEmail(eq("/send-email"), sendEmailCaptor.capture());
        verify(privateSendEmailPost, times(2)).execute();

        SendEmail sendEmail1 = sendEmailCaptor.getAllValues().getFirst();
        assertEquals("testAppId", sendEmail1.getAppId());
        assertEquals("teamType", sendEmail1.getMessageType());
        assertEquals("cardiff1@test.com", sendEmail1.getEmailAddress());

        SendEmail sendEmail2 = sendEmailCaptor.getAllValues().get(1);
        assertEquals("testAppId", sendEmail2.getAppId());
        assertEquals("teamType", sendEmail2.getMessageType());
        assertEquals("cardiff2@test.com", sendEmail2.getEmailAddress());
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
            assert (sendEmail.getJsonData().contains("user@test.com"));
            assert (sendEmail.getJsonData().contains("Objection for 12345678"));
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
            assert (sendEmail.getJsonData().contains("team@test.com"));
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
            org.mockito.Mockito.doThrow(new com.fasterxml.jackson.core.JsonProcessingException("fail") {
            }).when(mockMapper).writeValueAsString(any());
            objectMapperField.set(spyService, mockMapper);

            var m = EmailService.class.getDeclaredMethod("constructChsKafkaApiMessage", EmailType.class, String.class, Map.class);
            m.setAccessible(true);

            assertThrows(java.lang.reflect.InvocationTargetException.class,
                    () -> m.invoke(spyService, EmailType.CUSTOMER, email, data));
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
}