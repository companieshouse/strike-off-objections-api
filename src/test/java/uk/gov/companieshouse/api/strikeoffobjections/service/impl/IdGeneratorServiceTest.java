package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

import java.security.NoSuchAlgorithmException;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.when;

@Unit
@ExtendWith(MockitoExtension.class)
class IdGeneratorServiceTest {

    @Mock
    private Supplier<LocalDateTime> supplier;

    @InjectMocks
    private IdGeneratorService idGeneratorService;

    @Test
    void generateIdTest() {
        when(supplier.get()).thenReturn(LocalDateTime.of(2020, Month.OCTOBER, 6, 9, 30));
        String id = idGeneratorService.generateId();

        assertEquals("OBJ-", id.substring(0, 4));
        assertEquals(18, id.length());
    }
}
