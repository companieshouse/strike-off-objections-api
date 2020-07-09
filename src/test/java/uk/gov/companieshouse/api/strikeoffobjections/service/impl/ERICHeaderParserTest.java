package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ERICHeaderParserTest {

    private static final String E_MAIL = "demo@ch.gov.uk";
    private static final String AUTH_USER = E_MAIL + "; forename=demoForename; surname=demoSurname";
    private static final String UTF8_AUTH_USER = E_MAIL + "; forename*=UTF-8''demo%20%3BForename; surname*=UTF-8''demo%3BSurname";

    private final ERICHeaderParser ericHeaderParser = new ERICHeaderParser();

    @Test
    public void testGettingEmailAddressFromEricUserDetails() {
        assertEquals(E_MAIL, ericHeaderParser.getEmailAddress(AUTH_USER));
    }

    @Test
    public void testGettingEmailAddressFromEricUserDetailsInUTF8Format() {
        assertEquals(E_MAIL, ericHeaderParser.getEmailAddress(UTF8_AUTH_USER));
    }

    @Test
    public void testGettingEmailAddressFromEricUserDetailsWhenNotSet() {
        assertNull(ericHeaderParser.getEmailAddress(null));
        assertNull(ericHeaderParser.getEmailAddress(""));
        assertNull(ericHeaderParser.getEmailAddress("  "));
    }
}
