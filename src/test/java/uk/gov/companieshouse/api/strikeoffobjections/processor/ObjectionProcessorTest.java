package uk.gov.companieshouse.api.strikeoffobjections.processor;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.InvalidObjectionStatusException;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.repository.ObjectionRepository;
import uk.gov.companieshouse.api.strikeoffobjections.service.IChipsService;
import uk.gov.companieshouse.api.strikeoffobjections.service.ICompanyProfileService;
import uk.gov.companieshouse.api.strikeoffobjections.service.IEmailService;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.service.ServiceException;

@Unit
@ExtendWith(MockitoExtension.class)
class ObjectionProcessorTest {

    private static final String OBJECTION_ID = "87651234";
    private static final String HTTP_REQUEST_ID = "564565";
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    private static final String COMPANY_NAME = "Company: " + COMPANY_NUMBER;
    private static final String JURISDICTION = "wales";
    private static final String EMAIL = "demo@ch.gov.uk";
    private static final String USER_ID = "32324";
    private static final String REASON = "THIS IS A REASON";
    private static final String FULL_NAME = "Joe Bloggs";
    private static final LocalDateTime LOCAL_DATE_TIME =
            LocalDateTime.of(2020, 12, 10, 8, 0);

    @Mock
    private ApiLogger apiLogger;

    @Mock
    private IEmailService emailService;

    @Mock
    private ICompanyProfileService companyProfileService;

    @Mock
    private IChipsService chipsService;

    @Mock
    private ObjectionRepository objectionRepository;

    @InjectMocks
    private ObjectionProcessor objectionProcessor;

    @Test
    void processTest() throws Exception {
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, HTTP_REQUEST_ID))
                .thenReturn(Utils.getDummyCompanyProfile(COMPANY_NUMBER, JURISDICTION));
        Objection dummyObjection = Utils.getTestObjection(
                OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL, LOCAL_DATE_TIME,
                Utils.buildTestObjectionCreate(FULL_NAME, false));
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        ArgumentCaptor<Objection> objectionArgumentCaptor = ArgumentCaptor.forClass(Objection.class);
        assertDoesNotThrow(() -> objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID));
        verify(objectionRepository, times(1)).save(objectionArgumentCaptor.capture());

        Objection objection = objectionArgumentCaptor.getValue();
        assertEquals(ObjectionStatus.PROCESSED, objection.getStatus());
    }

    @Test
    void processOrderTest() throws Exception {
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, HTTP_REQUEST_ID))
                .thenReturn(Utils.getDummyCompanyProfile(COMPANY_NUMBER, JURISDICTION));
        Objection dummyObjection = Utils.getTestObjection(
                OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL, LOCAL_DATE_TIME,
                Utils.buildTestObjectionCreate(FULL_NAME, false));
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        assertDoesNotThrow(() -> objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID));

        InOrder processingOrder = Mockito.inOrder(companyProfileService, chipsService, emailService, objectionRepository);
        processingOrder.verify(companyProfileService, times(1)).getCompanyProfile(COMPANY_NUMBER,  HTTP_REQUEST_ID);
        processingOrder.verify(chipsService, times(1)).sendObjection(HTTP_REQUEST_ID, dummyObjection);
        processingOrder.verify(emailService, times(1))
                .sendObjectionSubmittedDissolutionTeamEmail(COMPANY_NAME, JURISDICTION,
                        dummyObjection, HTTP_REQUEST_ID);
        processingOrder.verify(emailService, times(1))
                .sendObjectionSubmittedCustomerEmail(dummyObjection, COMPANY_NAME, HTTP_REQUEST_ID);
        processingOrder.verify(objectionRepository, times(1)).save(dummyObjection);

    }

    @Test
    void processThrowsInvalidObjectionStatusTest() {
        Objection dummyObjection = Utils.getTestObjection(
                OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL, LOCAL_DATE_TIME,
                Utils.buildTestObjectionCreate(FULL_NAME, false));
        dummyObjection.setStatus(ObjectionStatus.OPEN);

        assertThrows(InvalidObjectionStatusException.class,
                () -> objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID));
    }

    @Test
    void processThrowsIllegalArgumentExceptionTest() {
        assertThrows(IllegalArgumentException.class,
                () -> objectionProcessor.process(null, HTTP_REQUEST_ID));

        Objection blankObjection = new Objection();
        assertThrows(IllegalArgumentException.class,
                () -> objectionProcessor.process(blankObjection, null));
    }

    @Test
    void processThrowsServiceExceptionIfUserDataIsMissingReason() {
        Objection dummyObjection = Utils.getTestObjection(
                OBJECTION_ID, "", COMPANY_NUMBER, USER_ID, EMAIL, LOCAL_DATE_TIME,
                Utils.buildTestObjectionCreate(FULL_NAME, false));
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        processObjectionAndCheckForError(dummyObjection);
    }

    @Test
    void processThrowsServiceExceptionIfUserDataIsMissingFullName() {
        Objection dummyObjection = Utils.getTestObjection(
                OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL, LOCAL_DATE_TIME,
                Utils.buildTestObjectionCreate("", false));
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        processObjectionAndCheckForError(dummyObjection);
    }

    @Test
    void processThrowsServiceExceptionIfUserDataIsMissingAttachments() {
        Objection dummyObjection = Utils.getTestObjection(
                OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL, LOCAL_DATE_TIME,
                Utils.buildTestObjectionCreate(FULL_NAME, false));
        dummyObjection.getAttachments().clear();
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        processObjectionAndCheckForError(dummyObjection);
    }

    @Test
    void processSendsDissolutionTeamEmail() throws Exception {
        Objection dummyObjection = Utils.getTestObjection(
                OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL, LOCAL_DATE_TIME,
                Utils.buildTestObjectionCreate(FULL_NAME, false));
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER,  HTTP_REQUEST_ID))
                .thenReturn(Utils.getDummyCompanyProfile(COMPANY_NUMBER, JURISDICTION));

        objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID);

        ArgumentCaptor<Objection> objectionArgumentCaptor = ArgumentCaptor.forClass(Objection.class);

        verify(emailService, times(1))
                .sendObjectionSubmittedDissolutionTeamEmail(COMPANY_NAME, JURISDICTION,
                        dummyObjection, HTTP_REQUEST_ID);

        verify(objectionRepository, times(1)).save(objectionArgumentCaptor.capture());

        Objection objection = objectionArgumentCaptor.getValue();
        assertEquals(ObjectionStatus.PROCESSED, objection.getStatus());
    }

    @Test
    void processHandlesDissolutionTeamEmailException() throws ServiceException {
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, HTTP_REQUEST_ID))
                .thenReturn(Utils.getDummyCompanyProfile(COMPANY_NUMBER, JURISDICTION));

        Objection dummyObjection = Utils.getTestObjection(
                OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL, LOCAL_DATE_TIME,
                Utils.buildTestObjectionCreate(FULL_NAME, false));
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        doThrow(new ServiceException("blah")).when(emailService).sendObjectionSubmittedDissolutionTeamEmail(any(), any(), any(), any());

        ArgumentCaptor<Objection> objectionArgumentCaptor = ArgumentCaptor.forClass(Objection.class);
        assertThrows(ServiceException.class, () -> objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID));

        verify(apiLogger, times(1)).errorContext(eq(HTTP_REQUEST_ID), any(), any(), any());

        verify(objectionRepository, times(1)).save(objectionArgumentCaptor.capture());

        Objection objection = objectionArgumentCaptor.getValue();
        assertEquals(ObjectionStatus.ERROR_INTERNAL_EMAIL, objection.getStatus());
    }

    @Test
    void processHandlesDissolutionTeamEmailUncheckedException() throws ServiceException {
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, HTTP_REQUEST_ID))
                .thenReturn(Utils.getDummyCompanyProfile(COMPANY_NUMBER, JURISDICTION));

        Objection dummyObjection = Utils.getTestObjection(
                OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL, LOCAL_DATE_TIME,
                Utils.buildTestObjectionCreate(FULL_NAME, false));
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        doThrow(new RuntimeException("blah")).when(emailService).sendObjectionSubmittedDissolutionTeamEmail(any(), any(), any(), any());

        ArgumentCaptor<Objection> objectionArgumentCaptor = ArgumentCaptor.forClass(Objection.class);
        assertThrows(RuntimeException.class, () -> objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID));

        verify(apiLogger, times(1)).errorContext(eq(HTTP_REQUEST_ID), any(), any(), any());

        verify(objectionRepository, times(1)).save(objectionArgumentCaptor.capture());

        Objection objection = objectionArgumentCaptor.getValue();
        assertEquals(ObjectionStatus.ERROR_INTERNAL_EMAIL, objection.getStatus());
    }

    @Test
    void processCallsChipsAndSendsCustomerEmail() throws Exception {
        Objection dummyObjection = Utils.getTestObjection(
                OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL, LOCAL_DATE_TIME,
                Utils.buildTestObjectionCreate(FULL_NAME, false));
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER,  HTTP_REQUEST_ID))
                .thenReturn(Utils.getDummyCompanyProfile(COMPANY_NUMBER, JURISDICTION));

        ArgumentCaptor<Objection> objectionArgumentCaptor = ArgumentCaptor.forClass(Objection.class);
        objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID);

        verify(chipsService, times(1)).sendObjection(HTTP_REQUEST_ID, dummyObjection);
        verify(emailService, times(1))
                .sendObjectionSubmittedCustomerEmail(dummyObjection, COMPANY_NAME, HTTP_REQUEST_ID);

        verify(objectionRepository, times(1)).save(objectionArgumentCaptor.capture());

        Objection objection = objectionArgumentCaptor.getValue();
        assertEquals(ObjectionStatus.PROCESSED, objection.getStatus());
    }

    @Test
    void processHandlesCustomerEmailException() throws ServiceException {
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, HTTP_REQUEST_ID))
                .thenReturn(Utils.getDummyCompanyProfile(COMPANY_NUMBER, JURISDICTION));

        Objection dummyObjection = Utils.getTestObjection(
                OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL, LOCAL_DATE_TIME,
                Utils.buildTestObjectionCreate(FULL_NAME, false));
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        doThrow(new ServiceException("blah")).when(emailService).sendObjectionSubmittedCustomerEmail(any(), any(), any());

        ArgumentCaptor<Objection> objectionArgumentCaptor = ArgumentCaptor.forClass(Objection.class);
        assertThrows(ServiceException.class, () -> objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID));

        verify(apiLogger, times(1)).errorContext(eq(HTTP_REQUEST_ID), any(), any(), any());

        verify(objectionRepository, times(1)).save(objectionArgumentCaptor.capture());

        Objection objection = objectionArgumentCaptor.getValue();
        assertEquals(ObjectionStatus.ERROR_EXTERNAL_EMAIL, objection.getStatus());
    }

    @Test
    void processHandlesCustomerEmailUncheckedException() throws ServiceException {
        when(companyProfileService.getCompanyProfile(COMPANY_NUMBER, HTTP_REQUEST_ID))
                .thenReturn(Utils.getDummyCompanyProfile(COMPANY_NUMBER, JURISDICTION));

        Objection dummyObjection = Utils.getTestObjection(
                OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL, LOCAL_DATE_TIME,
                Utils.buildTestObjectionCreate(FULL_NAME, false));
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        ArgumentCaptor<Objection> objectionArgumentCaptor = ArgumentCaptor.forClass(Objection.class);
        doThrow(new RuntimeException("blah")).when(emailService).sendObjectionSubmittedCustomerEmail(any(), any(), any());

        assertThrows(RuntimeException.class, () -> objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID));

        verify(apiLogger, times(1)).errorContext(eq(HTTP_REQUEST_ID), any(), any(), any());

        verify(objectionRepository, times(1)).save(objectionArgumentCaptor.capture());

        Objection objection = objectionArgumentCaptor.getValue();
        assertEquals(ObjectionStatus.ERROR_EXTERNAL_EMAIL, objection.getStatus());
    }

    @Test
    void processHandlesChipsUncheckedException() throws ServiceException {
        Objection dummyObjection = Utils.getTestObjection(
                OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL, LOCAL_DATE_TIME,
                Utils.buildTestObjectionCreate(FULL_NAME, false));
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        doThrow(new RuntimeException("blah")).when(chipsService).sendObjection(any(), any());

        ArgumentCaptor<Objection> objectionArgumentCaptor = ArgumentCaptor.forClass(Objection.class);
        assertThrows(RuntimeException.class, () -> objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID));

        verify(apiLogger, times(1)).errorContext(eq(HTTP_REQUEST_ID), any(), any(), any());

        verify(objectionRepository, times(1)).save(objectionArgumentCaptor.capture());

        Objection objection = objectionArgumentCaptor.getValue();
        assertEquals(ObjectionStatus.ERROR_CHIPS, objection.getStatus());
    }

    private void processObjectionAndCheckForError(Objection dummyObjection) {
        ArgumentCaptor<Objection> objectionArgumentCaptor = ArgumentCaptor.forClass(Objection.class);
        assertThrows(ServiceException.class,
                () -> objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID));
        
        verify(apiLogger, times(1)).errorContext(eq(HTTP_REQUEST_ID), any(), any(), any());
        verify(objectionRepository, times(1)).save(objectionArgumentCaptor.capture());

        Objection objection = objectionArgumentCaptor.getValue();
        assertEquals(ObjectionStatus.ERROR_DATA_INCOMPLETE, objection.getStatus());
    }
}
