package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.repository.ObjectionRepository;

import java.time.LocalDateTime;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
class ObjectionServiceTest {

    private static final String COMPANY_NUMBER = "12345678";
    private static final String REQUEST_ID = "87654321";
    private static final String OBJECTION_ID = "87651234";
    private static final LocalDateTime MOCKED_TIME_STAMP = LocalDateTime.of(2020, 2,2, 0, 0);

    @Mock
    ApiLogger apiLogger;

    @Mock
    ObjectionRepository objectionRepository;

    @Mock
    Supplier<LocalDateTime> localDateTimeSupplier;

    @InjectMocks
    ObjectionService objectionService;

    @Test
    void createObjectionTest() throws Exception{
        Objection returnedEntity = new Objection.Builder()
                .withCompanyNumber(COMPANY_NUMBER)
                .build();
        returnedEntity.setId(OBJECTION_ID);
        when(objectionRepository.save(any())).thenReturn(returnedEntity);
        when(localDateTimeSupplier.get()).thenReturn(MOCKED_TIME_STAMP);

        ArgumentCaptor<Objection> acObjection = ArgumentCaptor.forClass(Objection.class);
        String returnedId = objectionService.createObjection(REQUEST_ID, COMPANY_NUMBER);

        verify(objectionRepository).save(acObjection.capture());
        assertEquals(OBJECTION_ID, returnedId);
        assertEquals(MOCKED_TIME_STAMP, acObjection.getValue().getCreatedOn());
        assertEquals(COMPANY_NUMBER, acObjection.getValue().getCompanyNumber());
    }
}