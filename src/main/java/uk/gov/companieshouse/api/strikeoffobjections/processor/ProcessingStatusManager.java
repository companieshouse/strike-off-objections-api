package uk.gov.companieshouse.api.strikeoffobjections.processor;

import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import static uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus.ERROR_CHIPS;
import static uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus.ERROR_EXT_EMAIL;
import static uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus.ERROR_INT_EMAIL;
import static uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus.RETRY_CHIPS_ONLY;
import static uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus.RETRY_EXT_EMAIL_ONLY;
import static uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus.RETRY_INT_EMAIL_ONLY;
import static uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus.SUBMITTED;

@Component
public class ProcessingStatusManager {
    private static final Set<ObjectionStatus> CAN_PROCESS_CHIPS_STATUS =
            new HashSet<>(Arrays.asList(
                    SUBMITTED,
                    ERROR_CHIPS,
                    RETRY_CHIPS_ONLY));
    private static final Set<ObjectionStatus> CAN_SEND_INTERNAL_EMAIL_STATUS =
            new HashSet<>(Arrays.asList(
                    SUBMITTED,
                    ERROR_CHIPS,
                    ERROR_INT_EMAIL,
                    RETRY_INT_EMAIL_ONLY));
    private static final Set<ObjectionStatus> CAN_SEND_EXTERNAL_EMAIL_STATUS =
            new HashSet<>(Arrays.asList(
                    SUBMITTED,
                    ERROR_CHIPS,
                    ERROR_INT_EMAIL,
                    ERROR_EXT_EMAIL,
                    RETRY_EXT_EMAIL_ONLY));

    // TODO UNIT TESTS

    public boolean canProcessChips(ObjectionStatus status) {
        return CAN_PROCESS_CHIPS_STATUS.contains(status);
    }

    public boolean canSendInternalEmail(ObjectionStatus status) {
        return CAN_SEND_INTERNAL_EMAIL_STATUS.contains(status);
    }

    public boolean canSendExternalEmail(ObjectionStatus status) {
        return CAN_SEND_EXTERNAL_EMAIL_STATUS.contains(status);
    }
}
