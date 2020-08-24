package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

public enum ActionCode {
    IR_HMO_OBJECTION_IN_FORCE(4100),
    ORDINARY_OBJECTION_IN_FORCE(4300),
    IR_OR_HMO_AND_ORDINARY_OBJECTION_IN_FORCE(4400),
    FIRST_GAZETTE_ISSUED(5000),
    STRUCK_OFF_DISSOLVED(90),
    SECOND_GAZETTE_ISSUED(9000),
    CONVERTED_CLOSED(9100);

    private int codeId;

    private ActionCode(int codeId) {
        this.codeId = codeId;
    }

    public int getCodeId() {
        return codeId;
    }
}
