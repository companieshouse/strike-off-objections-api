package uk.gov.companieshouse.api.strikeoffobjections.common;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class FormatUtils {
    private static final String CREATE_AT_FORMAT = "dd MMM yyyy HH:mm:ss";
    private static final String DATE_FORMAT = "dd MMMM yyyy";

    private FormatUtils() {}

    public static String formatTimestamp(LocalDateTime timestamp) {
        return timestamp.format(DateTimeFormatter.ofPattern(CREATE_AT_FORMAT));
    }

    public static String formatDate(LocalDate date) {
        return date.format(DateTimeFormatter.ofPattern(DATE_FORMAT));
    }
}
