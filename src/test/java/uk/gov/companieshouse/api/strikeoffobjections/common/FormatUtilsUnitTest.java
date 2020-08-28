package uk.gov.companieshouse.api.strikeoffobjections.common;

import org.junit.jupiter.api.Test;
import uk.gov.companieshouse.api.strikeoffobjections.common.FormatUtils;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Unit
public class FormatUtilsUnitTest {

    @Test
    public void testFormatTimestamp() {
        LocalDateTime timestamp = LocalDateTime.of(2020,1,1,3,10,45);
        String result = FormatUtils.formatTimestamp(timestamp);
        assertEquals("01 Jan 2020 03:10:45", result);
    }

    @Test
    public void testFormatDate() {
        LocalDate date = LocalDate.of(2020,1,1);
        String result = FormatUtils.formatDate(date);
        assertEquals("01 January 2020", result);
    }
}
