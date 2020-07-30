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
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

@ExtendWith(MockitoExtension.class)
class ObjectionProcessorTest {

    private static final String OBJECTION_ID = "87651234";
    private static final String HTTP_REQUEST_ID = "564565";

    @Mock
    private ApiLogger apiLogger;

    @InjectMocks
    private ObjectionProcessor objectionProcessor;

    @Test
    void processTest() {
        Objection dummyObjection = Utils.getTestObjection(OBJECTION_ID, "", null);
        dummyObjection.setStatus(ObjectionStatus.SUBMITTED);

        assertDoesNotThrow(() -> objectionProcessor.process(dummyObjection, HTTP_REQUEST_ID));
    }

    @Test
    void processThrowsInvalidObjectionStatusTest() {
        Objection dummyObjection = Utils.getTestObjection(OBJECTION_ID, "", null);
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
}
