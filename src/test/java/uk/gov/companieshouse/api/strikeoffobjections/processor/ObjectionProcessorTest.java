package uk.gov.companieshouse.api.strikeoffobjections.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ObjectionProcessorTest {

    private static final String OBJECTION_ID = "87651234";
    private static final String HTTP_REQUEST_ID = "564565";

    @Mock
    private IObjectionService objectionService;

    @Mock
    private ApiLogger apiLogger;

    @InjectMocks
    private ObjectionProcessor objectionProcessor;

    @Test
    void processTest() throws ObjectionNotFoundException, InvalidObjectionStatusException {
        Objection dummyObjection = Utils.getTestObjection(OBJECTION_ID);
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        when(objectionService.getObjection(HTTP_REQUEST_ID, OBJECTION_ID)).thenReturn(dummyObjection);

        objectionProcessor.process(HTTP_REQUEST_ID, OBJECTION_ID);

        verify(objectionService, times(1)).getObjection(HTTP_REQUEST_ID, OBJECTION_ID);
    }

    @Test
    void processThrowsObjectionNotFoundExceptionTest() throws ObjectionNotFoundException {
        when(objectionService.getObjection(HTTP_REQUEST_ID, OBJECTION_ID))
                .thenThrow(new ObjectionNotFoundException("not found"));

        assertThrows(ObjectionNotFoundException.class,
                () -> objectionProcessor.process(HTTP_REQUEST_ID, OBJECTION_ID));
    }

    @Test
    void processThrowsInvalidObjectionStatusTest() throws ObjectionNotFoundException {
        Objection dummyObjection = Utils.getTestObjection(OBJECTION_ID);
        dummyObjection.setStatus(ObjectionStatus.OPEN);

        when(objectionService.getObjection(HTTP_REQUEST_ID, OBJECTION_ID)).thenReturn(dummyObjection);

        assertThrows(InvalidObjectionStatusException.class,
                () -> objectionProcessor.process(HTTP_REQUEST_ID, OBJECTION_ID));
    }
}
