package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ObjectionStatusTest {

    @Test
    void testWhenEligible() {
        ObjectionStatus objectionStatus = ObjectionStatus.OPEN;
        assertFalse(objectionStatus.isIneligible());
    }

    @Test
    void testWhenIneligibleStruckOff() {
        ObjectionStatus objectionStatus = ObjectionStatus.INELIGIBLE_COMPANY_STRUCK_OFF;
        assertTrue(objectionStatus.isIneligible());
    }

    @Test
    void testWhenIneligibleNoDissolutionAction() {
        ObjectionStatus objectionStatus = ObjectionStatus.INELIGIBLE_NO_DISSOLUTION_ACTION;
        assertTrue(objectionStatus.isIneligible());
    }
}
