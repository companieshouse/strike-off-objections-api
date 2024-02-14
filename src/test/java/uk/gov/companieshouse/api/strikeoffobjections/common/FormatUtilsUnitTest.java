package uk.gov.companieshouse.api.strikeoffobjections.common;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

@Unit
class FormatUtilsUnitTest {

    @Test
    void testFormatTimestamp() {
        var timestamp = LocalDateTime.of(2020, 1, 1, 3, 10, 45);
        String result = FormatUtils.formatTimestamp(timestamp);
        assertEquals("01 Jan 2020 03:10:45", result);
    }

    @Test
    void testFormatDate() {
        var date = LocalDate.of(2020, 1, 1);
        String result = FormatUtils.formatDate(date);
        assertEquals("01 January 2020", result);
    }
}
