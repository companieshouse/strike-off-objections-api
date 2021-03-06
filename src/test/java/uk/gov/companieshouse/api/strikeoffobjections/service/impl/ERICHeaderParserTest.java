package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

@Unit
class ERICHeaderParserTest {

    private static final String EMAIL = "demo@ch.gov.uk";
    private static final String AUTH_USER = EMAIL + "; forename=demoForename; surname=demoSurname";
    private static final String UTF8_AUTH_USER = EMAIL + "; forename*=UTF-8''demo%20%3BForename; surname*=UTF-8''demo%3BSurname";

    private final ERICHeaderParser ericHeaderParser = new ERICHeaderParser();

    @Test
    void testGettingEmailAddressFromEricUserDetails() {
        assertEquals(EMAIL, ericHeaderParser.getEmailAddress(AUTH_USER));
    }

    @Test
    void testGettingEmailAddressFromEricUserDetailsInUTF8Format() {
        assertEquals(EMAIL, ericHeaderParser.getEmailAddress(UTF8_AUTH_USER));
    }

    @Test
    void testGettingEmailAddressFromEricUserDetailsWhenNotSet() {
        assertNull(ericHeaderParser.getEmailAddress(null));
        assertNull(ericHeaderParser.getEmailAddress(""));
        assertNull(ericHeaderParser.getEmailAddress("  "));
    }
}
