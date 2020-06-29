package uk.gov.companieshouse.api.strikeoffobjections.model.request;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StrikeOffObjectionsRequestEntityTest {

    private static final Supplier<LocalDateTime> MOCK_PRESENT = () -> LocalDateTime.of(2020, 6, 30, 10, 00);

    @Test
    public void test() {
       StrikeOffObjectionsRequestEntity strikeOffObjectionsRequestEntity =
               new StrikeOffObjectionsRequestEntity.Builder()
                       .withRequestId("1")
                       .withUsername("Joe Bloggs")
                       .withEMail("jBloggs@ch.gov.uk")
                       .withRequestInformation("This is a test")
                       .withCreatedOn(MOCK_PRESENT.get())
                       .withStatus(RequestStatus.OPEN)
                       .build();

       assertEquals("1", strikeOffObjectionsRequestEntity.getRequestId());
       assertEquals("Joe Bloggs", strikeOffObjectionsRequestEntity.getUsername());
       assertEquals("jBloggs@ch.gov.uk", strikeOffObjectionsRequestEntity.getEMail());
       assertEquals("This is a test", strikeOffObjectionsRequestEntity.getRequestInformation());
       assertEquals(MOCK_PRESENT.get(), strikeOffObjectionsRequestEntity.getCreatedOn());
       assertEquals(RequestStatus.OPEN, strikeOffObjectionsRequestEntity.getStatus());
    }
}
