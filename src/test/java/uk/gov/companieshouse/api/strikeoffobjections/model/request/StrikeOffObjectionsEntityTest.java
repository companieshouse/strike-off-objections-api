package uk.gov.companieshouse.api.strikeoffobjections.model.request;

import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class StrikeOffObjectionsEntityTest {

    private static final LocalDateTime MOCK_PRESENT = LocalDateTime.of(2020, 6, 30, 10, 00);

    @Test
    public void enityBuilderTest() {

       CreatedBy createdBy = new CreatedBy();
       createdBy.setId("1");
       createdBy.setForename("Joe");
       createdBy.setSurname("Bloggs");
       createdBy.setEmailAddress("jBloggs@ch.gov.uk");

       StrikeOffObjectionsEntity strikeOffObjectionsEntity =
               new StrikeOffObjectionsEntity.Builder()
                       .withUsername("Joe Bloggs")
                       .withEMailAddress("jBloggs@ch.gov.uk")
                       .withReason("This is a test")
                       .withCreatedBy(createdBy)
                       .withCreatedOn(MOCK_PRESENT)
                       .withStatus(RequestStatus.OPEN)
                       .build();

       assertEquals("Joe Bloggs", strikeOffObjectionsEntity.getUsername());
       assertEquals("jBloggs@ch.gov.uk", strikeOffObjectionsEntity.getEMailAddress());
       assertEquals("This is a test", strikeOffObjectionsEntity.getReason());
       assertEquals("1", strikeOffObjectionsEntity.getCreatedBy().getId());
       assertEquals("Joe", strikeOffObjectionsEntity.getCreatedBy().getForename());
       assertEquals("Bloggs", strikeOffObjectionsEntity.getCreatedBy().getSurname());
       assertEquals("jBloggs@ch.gov.uk", strikeOffObjectionsEntity.getCreatedBy().getEmailAddress());
       assertEquals(MOCK_PRESENT, strikeOffObjectionsEntity.getCreatedOn());
       assertEquals(RequestStatus.OPEN, strikeOffObjectionsEntity.getStatus());
    }
}