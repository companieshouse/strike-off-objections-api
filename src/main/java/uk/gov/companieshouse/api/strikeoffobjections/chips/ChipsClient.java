package uk.gov.companieshouse.api.strikeoffobjections.chips;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;

import java.util.HashMap;
import java.util.Map;

@Component
public class ChipsClient {

    private final ApiLogger apiLogger;
    private final RestTemplate restTemplate;

    //TODO Re-enable chips call
    //@Value("${OBJECT_TO_STRIKE_OFF_CHIPS_REST_INTERFACE_URL}")
    private String chipsRestUrl;

    @Autowired
    public ChipsClient(ApiLogger apiLogger, RestTemplate restTemplate) {
        this.apiLogger = apiLogger;
        this.restTemplate = restTemplate;
    }

    public void sendToChips(String requestId, ChipsRequest chipsRequest){
        Map<String, Object> logMap = new HashMap<>();
        logMap.put("chipsRestUrl", chipsRestUrl);

        apiLogger.infoContext(
                requestId,
                String.format("Posting %s to CHIPS rest interfaces", chipsRequest),
                logMap
        );

        //TODO Re-enable chips call
        /**
        ResponseEntity<String> chipsRestResponse = restTemplate.postForEntity(chipsRestUrl, chipsRequest, String.class);

        apiLogger.infoContext(
                requestId,
                String.format("Sent data to CHIPS, received status code: %s", chipsRestResponse.getStatusCode())
        );
         **/
    }
}
