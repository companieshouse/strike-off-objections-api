package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

import org.junit.jupiter.api.Test;

import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.service.links.Links;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Unit
class ObjectionTest {

    private static final LocalDateTime MOCK_PRESENT = LocalDateTime.of(2020, 6, 30, 10, 00);

    @Test
    void enityBuilderTest() {

       CreatedBy createdBy = new CreatedBy("1", "jBloggs@ch.gov.uk",
               "client", "Joe Bloggs", false);

       Map<String, String> linksMap = new HashMap<>();
       linksMap.put("download", "/abc/download");
       linksMap.put("self", "abc/self");

       Links links = new Links();
       links.setLinks(linksMap);

       Attachment attachment = new Attachment();
       attachment.setId("1");
       attachment.setLinks(links);
       attachment.setName("test.jpg");
       attachment.setContentType("image/jpeg");
       attachment.setSize(5000L);

       Objection objection =
               new Objection.Builder()
                       .withCreatedOn(MOCK_PRESENT)
                       .withCreatedBy(createdBy)
                       .withCompanyNumber("00006400")
                       .withReason("This is a test")
                       .withStatus(ObjectionStatus.OPEN)
                       .withActionCode(4400L)
                       .build();

       objection.addAttachment(attachment);

       assertEquals(MOCK_PRESENT, objection.getCreatedOn());
       assertEquals("1", objection.getCreatedBy().getId());
       assertEquals("jBloggs@ch.gov.uk", objection.getCreatedBy().getEmail());
       assertEquals("00006400", objection.getCompanyNumber());

       assertEquals("1", objection.getAttachments().get(0).getId());
       assertEquals("/abc/download", objection.getAttachments().get(0).getLinks().getLink(ObjectionLinkKeys.DOWNLOAD));
       assertEquals("abc/self", objection.getAttachments().get(0).getLinks().getLink(ObjectionLinkKeys.SELF));
       assertEquals("test.jpg", objection.getAttachments().get(0).getName());
       assertEquals("image/jpeg", objection.getAttachments().get(0).getContentType());
       assertEquals(5000L, objection.getAttachments().get(0).getSize());

       assertEquals("This is a test", objection.getReason());
       assertEquals(ObjectionStatus.OPEN, objection.getStatus());
       assertEquals(4400L, objection.getActionCode());
    }
}
