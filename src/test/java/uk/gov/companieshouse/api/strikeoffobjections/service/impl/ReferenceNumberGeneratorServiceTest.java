package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

import java.time.LocalDateTime;
import java.time.Month;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class ReferenceNumberGeneratorServiceTest {

    @Mock
    private Supplier<LocalDateTime> supplier;

    @InjectMocks
    private ReferenceNumberGeneratorService referenceNumberGeneratorService;

    @Test
    void generateIdTest() {
        when(supplier.get()).thenReturn(LocalDateTime.of(2020, Month.OCTOBER, 6, 9, 30));
        String referenceNumber = referenceNumberGeneratorService.generateReferenceNumber();

        assertEquals("OBJ-", referenceNumber.substring(0, 4));
        assertEquals(18, referenceNumber.length());
    }
}
