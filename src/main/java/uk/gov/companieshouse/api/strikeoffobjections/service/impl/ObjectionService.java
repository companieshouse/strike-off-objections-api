package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.LogConstants;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.StrikeOffObjectionsEntity;
import uk.gov.companieshouse.api.strikeoffobjections.repository.StrikeOffObjectionsRepository;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@Service
public class ObjectionService implements IObjectionService {

    private StrikeOffObjectionsRepository strikeOffObjectionsRepository;
    private ApiLogger logger;

    @Autowired
    public ObjectionService(StrikeOffObjectionsRepository strikeOffObjectionsRepository, ApiLogger logger) {
        this.strikeOffObjectionsRepository = strikeOffObjectionsRepository;
        this.logger = logger;
    }

    @Override
    public String createObjection(String requestId, String companyNumber) {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), companyNumber);
        logger.infoContext(requestId, "Creating objection", logMap);

        StrikeOffObjectionsEntity entity = new StrikeOffObjectionsEntity.Builder()
                .withCompanyNumber(companyNumber)
                .withCreatedOn(LocalDateTime.now())
                .build();

        StrikeOffObjectionsEntity savedEntity = strikeOffObjectionsRepository.save(entity);
        return savedEntity.getId();
    }
}
