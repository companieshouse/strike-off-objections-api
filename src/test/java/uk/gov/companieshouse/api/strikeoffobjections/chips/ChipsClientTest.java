package uk.gov.companieshouse.api.strikeoffobjections.chips;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@Unit
@ExtendWith(MockitoExtension.class)
class ChipsClientTest {

    private static final String REQUEST_ID = "test123";
    private static final String COMPANY_NUMBER = "12345678";

    @InjectMocks
    private ChipsClient chipsClient;

    @Mock
    private ApiLogger apiLogger;

    @Test
    void testSendMessageToChips(){
        // TODO OBJ-240 test sending contact data to CHIPS and capture request content
        ChipsRequest chipsRequest = new ChipsRequest(
                REQUEST_ID,
                COMPANY_NUMBER
        );
        chipsClient.sendToChips(REQUEST_ID, chipsRequest);
        verify(apiLogger, times(1)).infoContext(anyString(), anyString());
    }
}
