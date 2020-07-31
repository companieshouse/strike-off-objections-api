package uk.gov.companieshouse.api.strikeoffobjections.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.InvalidObjectionStatusException;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.service.IEmailService;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.service.ServiceException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class ObjectionProcessorTest {

    private static final String OBJECTION_ID = "87651234";
    private static final String HTTP_REQUEST_ID = "564565";

    @Mock
    private ApiLogger apiLogger;

    @Mock
    private IEmailService emailService;

    @InjectMocks
    private ObjectionProcessor objectionProcessor;

    @Test
    void processTest() {
        Objection dummyObjection = Utils.getTestObjection(OBJECTION_ID);
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        assertDoesNotThrow(() -> objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID));
    }

    @Test
    void processThrowsInvalidObjectionStatusTest() {
        Objection dummyObjection = Utils.getTestObjection(OBJECTION_ID);
        dummyObjection.setStatus(ObjectionStatus.OPEN);

        assertThrows(InvalidObjectionStatusException.class,
                () -> objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID));
    }

    @Test
    void processThrowsIllegalArgumentExceptionTest() {
        assertThrows(IllegalArgumentException.class,
                () -> objectionProcessor.process(null, HTTP_REQUEST_ID));

        assertThrows(IllegalArgumentException.class,
                () -> objectionProcessor.process(new Objection(), null));
    }

    @Test
    void processSendsCustomerEmail() throws Exception {
        Objection dummyObjection = Utils.getTestObjection(OBJECTION_ID);
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID);

        verify(emailService, times(1))
                .sendObjectionSubmittedCustomerEmail(dummyObjection, HTTP_REQUEST_ID);
    }

    @Test
    void processHandlesCustomerEmailException() throws ServiceException {
        Objection dummyObjection = Utils.getTestObjection(OBJECTION_ID);
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        doThrow(new ServiceException("blah")).when(emailService).sendObjectionSubmittedCustomerEmail(any(), any());

        assertThrows(ServiceException.class, () -> objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID));

        verify(apiLogger, times(1)).errorContext(eq(HTTP_REQUEST_ID), any(), any(), any());
    }

    @Test
    void processHandlesCustomerEmailUncheckedException() throws ServiceException {
        Objection dummyObjection = Utils.getTestObjection(OBJECTION_ID);
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        doThrow(new RuntimeException("blah")).when(emailService).sendObjectionSubmittedCustomerEmail(any(), any());

        assertThrows(RuntimeException.class, () -> objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID));

        verify(apiLogger, times(1)).errorContext(eq(HTTP_REQUEST_ID), any(), any(), any());
    }
}
