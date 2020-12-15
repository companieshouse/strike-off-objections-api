package uk.gov.companieshouse.api.strikeoffobjections.chips;

import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;

public interface ChipsSender {
    void sendToChips(String requestId, ChipsRequest chipsRequest);
}
