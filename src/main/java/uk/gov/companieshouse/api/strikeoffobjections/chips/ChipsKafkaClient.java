package uk.gov.companieshouse.api.strikeoffobjections.chips;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;

@Component
@Profile("kafka")
public class ChipsKafkaClient implements ChipsSender {

    @Override
    public void sendToChips(String requestId, ChipsRequest chipsRequest) {
        System.out.println("Sending to Kafka");
    }
}
