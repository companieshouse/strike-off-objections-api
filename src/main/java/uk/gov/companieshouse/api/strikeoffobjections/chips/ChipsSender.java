package uk.gov.companieshouse.api.strikeoffobjections.chips;

import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;
import uk.gov.companieshouse.service.ServiceException;

public interface ChipsSender {
    void sendToChips(String requestId, ChipsRequest chipsRequest) throws ServiceException;
}
