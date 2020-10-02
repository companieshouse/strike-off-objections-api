package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import uk.gov.companieshouse.api.strikeoffobjections.chips.ChipsClient;
import uk.gov.companieshouse.api.strikeoffobjections.groups.Unit;
import uk.gov.companieshouse.api.strikeoffobjections.model.chips.ChipsRequest;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.utils.Utils;
import uk.gov.companieshouse.service.ServiceException;
import uk.gov.companieshouse.service.links.CoreLinkKeys;

@Unit
@ExtendWith(MockitoExtension.class)
public class ChipsServiceTest {

    private static final String REQUEST_ID = "REQUEST_ID";
    private static final String COMPANY_NUMBER = "COMPANY_NUMBER";
    private static final LocalDateTime LOCAL_DATE_TIME = LocalDateTime.of(2020, 12, 10, 8, 0);
    private static final String OBJECTION_ID = "OBJECTION_ID";
    private static final String EMAIL = "demo@ch.gov.uk";
    private static final String USER_ID = "32324";
    private static final String REASON = "THIS IS A REASON";

    @Mock
    private ChipsClient chipsClient;

    @InjectMocks
    private ChipsService chipsService;

    @Test
    void testSendingToChipsCreatesCorrectRequest() throws ServiceException {
        Objection objection = Utils.getTestObjection(
                OBJECTION_ID, REASON, COMPANY_NUMBER, USER_ID, EMAIL, LOCAL_DATE_TIME,
                Utils.buildTestObjectionCreate("Joe Bloggs", false));
        List<Attachment> attachments = new ArrayList<>();
        Utils.setTestAttachmentsWithLinks(attachments);
        objection.setAttachments(attachments);

        chipsService.sendObjection(REQUEST_ID, objection);
        ArgumentCaptor<ChipsRequest> chipsRequestArgumentCaptor = ArgumentCaptor.forClass(ChipsRequest.class);

        verify(chipsClient, times(1)).sendToChips(eq(REQUEST_ID), chipsRequestArgumentCaptor.capture());

        ChipsRequest chipsRequest = chipsRequestArgumentCaptor.getValue();

        assertEquals(COMPANY_NUMBER, chipsRequest.getCompanyNumber());
        assertEquals(OBJECTION_ID, chipsRequest.getObjectionId());
        assertEquals(EMAIL, chipsRequest.getCustomerEmail());
        assertEquals(REASON, chipsRequest.getReason());

        assertEquals("link to SELF 1",
                chipsRequest.getAttachments().get("TestAttachment1"));
        assertEquals("link to SELF 2",
                chipsRequest.getAttachments().get("TestAttachment2"));
    }
}
