package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class ReferenceNumberGeneratorServiceTest {

    private static final Set<Character> ALLOWED_CHARS = new HashSet<>
            (Arrays.asList('A','B','C','D','E','F','1','2','3','4','5','6','7','8','9','0', '-'));

    private static final LocalDateTime LOCAL_DATE_TIME =
            LocalDateTime.of(2020, Month.OCTOBER, 6, 9, 30);
    @Mock
    private Supplier<LocalDateTime> supplier;

    @InjectMocks
    private ReferenceNumberGeneratorService referenceNumberGeneratorService;

    @Test
    void generateIdTestLengthAndPrefix() {
        when(supplier.get()).thenReturn(LOCAL_DATE_TIME);
        String referenceNumber = referenceNumberGeneratorService.generateReferenceNumber();

        assertEquals("OBJ-", referenceNumber.substring(0, 4));
        assertEquals(18, referenceNumber.length());
    }

    @Test
    void generateIdTestOnlyAllowedCharsPresent() {
        when(supplier.get()).thenReturn(LOCAL_DATE_TIME);
        String referenceNumber = referenceNumberGeneratorService.generateReferenceNumber();
        String referenceNumberNoPrefix = referenceNumber.substring(4);

        for (int i = 0; i < referenceNumberNoPrefix.length(); i++) {
            assertTrue(ALLOWED_CHARS.contains(referenceNumberNoPrefix.charAt(i)));
        }
    }

    @Test
    void generateIdTestHyphensInCorrectPlace() {
        when(supplier.get()).thenReturn(LOCAL_DATE_TIME);
        String referenceNumber = referenceNumberGeneratorService.generateReferenceNumber();
        String referenceNumberNoPrefix = referenceNumber.substring(4);

        for (int i = 4; i < referenceNumberNoPrefix.length(); i+= 5) {
            assertEquals('-', referenceNumberNoPrefix.charAt(i));
        }
    }
}
