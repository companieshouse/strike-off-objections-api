package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

@Unit
@ExtendWith(MockitoExtension.class)
class ReferenceNumberGeneratorServiceTest {

    private static final Set<Character> ALLOWED_CHARS = new HashSet<>(Arrays.asList(
            'A', 'B', 'C', 'D', 'E', 'F', '1', '2', '3', '4', '5', '6', '7', '8', '9', '0', '-'));

    @InjectMocks
    private ReferenceNumberGeneratorService referenceNumberGeneratorService;

    @Test
    void generateIdTestLengthAndPrefix() {
        String referenceNumber = referenceNumberGeneratorService.generateReferenceNumber();

        assertEquals("OBJ-", referenceNumber.substring(0, 4));
        assertEquals(18, referenceNumber.length());
    }

    @Test
    void generateIdTestOnlyAllowedCharsPresent() {
        String referenceNumber = referenceNumberGeneratorService.generateReferenceNumber();
        var referenceNumberNoPrefix = referenceNumber.substring(4);

        for (var i = 0; i < referenceNumberNoPrefix.length(); i++) {
            assertTrue(ALLOWED_CHARS.contains(referenceNumberNoPrefix.charAt(i)));
        }
    }

    @Test
    void generateIdTestHyphensInCorrectPlace() {
        String referenceNumber = referenceNumberGeneratorService.generateReferenceNumber();
        var referenceNumberNoPrefix = referenceNumber.substring(4);

        for (var i = 4; i < referenceNumberNoPrefix.length(); i += 5) {
            assertEquals('-', referenceNumberNoPrefix.charAt(i));
        }
    }
}
