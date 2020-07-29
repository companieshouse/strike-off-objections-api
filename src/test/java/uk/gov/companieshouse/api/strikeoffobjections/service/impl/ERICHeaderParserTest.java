package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ERICHeaderParserTest {

    private static final String EMAIL = "demo@ch.gov.uk";
    private static final String AUTH_USER = EMAIL + "; forename=demoForename; surname=demoSurname";
    private static final String UTF8_AUTH_USER = EMAIL + "; forename*=UTF-8''demo%20%3BForename; surname*=UTF-8''demo%3BSurname";
    private static final String FULL_NAME = "demoForename demoSurname";
    private static final String UTF_8_FULL_NAME = "UTF-8''demo%20%3BForename UTF-8''demo%3BSurname";

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

    @Test
    void testGettingNameFromUserDetails() {
        assertEquals(FULL_NAME, ericHeaderParser.getFullName(AUTH_USER));
    }

    @Test
    void testGettingNameFromUserDetailsInUTF8Format() {
        assertEquals(UTF_8_FULL_NAME, ericHeaderParser.getFullName(UTF8_AUTH_USER));
    }

    @Test
    void testGettingFullNameFromEricUserDetailsWhenNotSet() {
        assertEquals("", ericHeaderParser.getFullName(null));
        assertEquals("", ericHeaderParser.getFullName(""));
        assertEquals("", ericHeaderParser.getFullName("    "));
    }
}
