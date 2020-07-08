package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.LogConstants;
import uk.gov.companieshouse.api.strikeoffobjections.exception.ObjectionNotFoundException;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.patcher.ObjectionPatcher;
import uk.gov.companieshouse.api.strikeoffobjections.model.patch.ObjectionPatch;
import uk.gov.companieshouse.api.strikeoffobjections.repository.ObjectionRepository;
import uk.gov.companieshouse.api.strikeoffobjections.service.IObjectionService;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Supplier;

@Service
public class ObjectionService implements IObjectionService {

    private ObjectionRepository objectionRepository;
    private ApiLogger logger;
    private Supplier<LocalDateTime> dateTimeSupplier;
    private ObjectionPatcher objectionPatcher;

    @Autowired
    public ObjectionService(ObjectionRepository objectionRepository, ApiLogger logger, Supplier<LocalDateTime> dateTimeSupplier, ObjectionPatcher objectionPatcher) {
        this.objectionRepository = objectionRepository;
        this.logger = logger;
        this.dateTimeSupplier = dateTimeSupplier;
        this.objectionPatcher = objectionPatcher;
    }

    @Override
    public String createObjection(String requestId, String companyNumber) throws Exception{
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), companyNumber);
        logger.infoContext(requestId, "Creating objection", logMap);

        Objection entity = new Objection.Builder()
                .withCompanyNumber(companyNumber)
                .withCreatedOn(dateTimeSupplier.get())
                .withHttpRequestId(requestId)
                .build();

        Objection savedEntity = objectionRepository.save(entity);
        return savedEntity.getId();
    }

    @Override
    public void patchObjection(String requestId, String companyNumber, String objectionId, ObjectionPatch objectionPatch) throws ObjectionNotFoundException {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), companyNumber);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), objectionId);
        logger.infoContext(requestId, "Checking for existing objection", logMap);

        Optional<Objection> existingObjection = objectionRepository.findById(objectionId);
        if (existingObjection.isPresent()) {
            logger.infoContext(requestId, "Objection exists, patching", logMap);
            Objection objection = objectionPatcher.patchObjection(objectionPatch, requestId, existingObjection.get());
            objectionRepository.save(objection);
        } else {
            logger.infoContext(requestId, "Objection does not exist", logMap);
            throw new ObjectionNotFoundException(String.format("Objection with id: %s, not found", objectionId));
        }
    }

    @Override
    public List<Attachment> getAttachments(String requestId, String companyNumber, String objectionId) throws ObjectionNotFoundException {
        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LogConstants.COMPANY_NUMBER.getValue(), companyNumber);
        logMap.put(LogConstants.OBJECTION_ID.getValue(), objectionId);
        logger.infoContext(requestId, "Finding the objection", logMap);

        Optional<Objection> objection = objectionRepository.findById(objectionId);
        if (objection.isPresent()) {
            logger.infoContext(requestId, "Objection exists, returning attachments", logMap);
            return objection.get().getAttachments();
        } else {
            logger.infoContext(requestId, "Objection does not exist", logMap);
            throw new ObjectionNotFoundException(String.format("Objection with id: %s, not found", objectionId));
        }
    }
}
