package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.StrikeOffObjectionsEntity;
import uk.gov.companieshouse.api.strikeoffobjections.repository.StrikeOffObjectionsRepository;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ObjectionServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String REQUEST_ID = "87654321";
    private static final String OBJECTION_ID = "87651234";

    @Mock
    ApiLogger apiLogger;

    @Mock
    StrikeOffObjectionsRepository strikeOffObjectionsRepository;

    @InjectMocks
    ObjectionService objectionService;

    @Test
    void createObjectionTest() {
        StrikeOffObjectionsEntity returnedEntity = new StrikeOffObjectionsEntity.Builder()
                .withCompanyNumber(COMPANY_NUMBER)
                .build();
        returnedEntity.setId(OBJECTION_ID);
        when(strikeOffObjectionsRepository.save(any())).thenReturn(returnedEntity);

        String returnedId = objectionService.createObjection(REQUEST_ID, COMPANY_NUMBER);

        assertEquals(OBJECTION_ID, returnedId);
    }
}