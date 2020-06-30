package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class ObjectionTest {

    private static final LocalDateTime MOCK_PRESENT = LocalDateTime.of(2020, 6, 30, 10, 00);

    @Test
    public void enityBuilderTest() {

       CreatedBy createdBy = new CreatedBy();
       createdBy.setId("1");
       createdBy.setEmail("jBloggs@ch.gov.uk");

       Objection objection =
               new Objection.Builder()
                       .withCreatedOn(MOCK_PRESENT)
                       .withCreatedBy(createdBy)
                       .withCompanyNumber("00006400")
                       .withReason("This is a test")
                       .withStatus(ObjectionStatus.OPEN)
                       .build();
       assertEquals(MOCK_PRESENT, objection.getCreatedOn());
       assertEquals("1", objection.getCreatedBy().getId());
       assertEquals("jBloggs@ch.gov.uk", objection.getCreatedBy().getEmail());
       assertEquals("00006400", objection.getCompanyNumber());
       assertEquals("This is a test", objection.getReason());
       assertEquals(ObjectionStatus.OPEN, objection.getStatus());
    }
}
