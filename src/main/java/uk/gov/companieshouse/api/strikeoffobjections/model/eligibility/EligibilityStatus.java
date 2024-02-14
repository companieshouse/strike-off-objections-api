package uk.gov.companieshouse.api.strikeoffobjections.model.eligibility;

import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;

public enum EligibilityStatus {
    ELIGIBLE(ObjectionStatus.OPEN),
    INELIGIBLE_COMPANY_STRUCK_OFF(ObjectionStatus.INELIGIBLE_COMPANY_STRUCK_OFF),
    INELIGIBLE_NO_DISSOLUTION_ACTION(ObjectionStatus.INELIGIBLE_NO_DISSOLUTION_ACTION),
    INELIGIBLE_GAZ2_REQUESTED(ObjectionStatus.INELIGIBLE_GAZ2_REQUESTED);

    private final ObjectionStatus objectionStatus;

    EligibilityStatus(ObjectionStatus objectionStatus) {
        this.objectionStatus = objectionStatus;
    }

    public ObjectionStatus getObjectionStatus() {
        return objectionStatus;
    }
}
