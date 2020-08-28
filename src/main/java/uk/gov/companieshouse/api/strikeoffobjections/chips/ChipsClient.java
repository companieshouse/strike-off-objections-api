package uk.gov.companieshouse.api.strikeoffobjections.chips;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;

@Component
public class ChipsClient {

    private ApiLogger apiLogger;

    @Autowired
    public ChipsClient(ApiLogger apiLogger) {
        this.apiLogger = apiLogger;
    }

    public void sendToChips(String requestId, ChipsRequest chipsRequest){
        // TODO OBJ-240 add contact model object and send to CHIPS
        apiLogger.infoContext(requestId,
                String.format("Posting %s to chips rest interfaces", chipsRequest));
    }
}
