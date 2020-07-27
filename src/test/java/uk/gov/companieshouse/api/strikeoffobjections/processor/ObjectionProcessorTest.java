package uk.gov.companieshouse.api.strikeoffobjections.processor;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ObjectionProcessorTest {

    private static final String OBJECTION_ID = "87651234";
    private static final String HTTP_REQUEST_ID = "564565";

    @Mock
    private IObjectionService objectionService;

    @InjectMocks
    private ObjectionProcessor objectionProcessor;

    @Test
    void processTest() throws ObjectionNotFoundException {
        objectionProcessor.process(HTTP_REQUEST_ID, OBJECTION_ID);

        verify(objectionService, times(1)).getObjection(HTTP_REQUEST_ID, OBJECTION_ID);
    }

    @Test
    void processThrowsNotFoundTest() throws ObjectionNotFoundException {
        when(objectionService.getObjection(HTTP_REQUEST_ID, OBJECTION_ID))
                .thenThrow(new ObjectionNotFoundException("not found"));

        assertThrows(ObjectionNotFoundException.class,
                () -> objectionProcessor.process(HTTP_REQUEST_ID, OBJECTION_ID));
    }
}
