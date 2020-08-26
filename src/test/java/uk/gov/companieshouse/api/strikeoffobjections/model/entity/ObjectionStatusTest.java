package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ObjectionStatusTest {

    @Test
    public void testWhenEligible() {
        ObjectionStatus objectionStatus = ObjectionStatus.OPEN;
        assertFalse(objectionStatus.isIneligibleStatus());
    }

    @Test
    public void testWhenIneligibleStruckOff() {
        ObjectionStatus objectionStatus = ObjectionStatus.INELIGIBLE_COMPANY_STRUCK_OFF;
        assertTrue(objectionStatus.isIneligibleStatus());
    }

    @Test
    public void testWhenIneligibleNoDissolutionAction() {
        ObjectionStatus objectionStatus = ObjectionStatus.INELIGIBLE_NO_DISSOLUTION_ACTION;
        assertTrue(objectionStatus.isIneligibleStatus());
    }
}
