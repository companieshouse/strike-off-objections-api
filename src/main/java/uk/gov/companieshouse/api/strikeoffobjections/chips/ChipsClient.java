package uk.gov.companieshouse.api.strikeoffobjections.chips;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;

@Component
public class ChipsClient {

    private ApiLogger apiLogger;

    @Autowired
    public ChipsClient(ApiLogger apiLogger) {
        this.apiLogger = apiLogger;
    }

    public void sendToChips(String requestId){
        // TODO OBJ-240 add contact model object and send to CHIPS
        apiLogger.infoContext(requestId, "Sending contact to CHIPS");
    }
}
