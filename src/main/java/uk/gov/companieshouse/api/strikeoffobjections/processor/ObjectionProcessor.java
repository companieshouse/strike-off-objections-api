package uk.gov.companieshouse.api.strikeoffobjections.processor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.model.company.CompanyProfileApi;
import uk.gov.companieshouse.api.strikeoffobjections.common.ApiLogger;
import uk.gov.companieshouse.api.strikeoffobjections.common.LogConstants;
import uk.gov.companieshouse.api.strikeoffobjections.exception.InvalidObjectionStatusException;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionStatus;
import uk.gov.companieshouse.api.strikeoffobjections.repository.ObjectionRepository;
import uk.gov.companieshouse.api.strikeoffobjections.service.ICompanyProfileService;
import uk.gov.companieshouse.api.strikeoffobjections.service.IEmailService;
import uk.gov.companieshouse.service.ServiceException;

import java.util.HashMap;
import java.util.Map;

/**
 * Processes an Objection
 * <p>
 * Will only process Objection if status = SUBMITTED
 * Calls Chips to place stop against company
 * Sends email
 */
@Component
public class ObjectionProcessor {

    private static final String INVALID_START_STATUS_MSG =
            "Objection %s has status %s. Cannot process unless status = SUBMITTED";
    private static final String LOG_OBJECTION_ID_KEY = LogConstants.OBJECTION_ID.getValue();

    private IEmailService emailService;
    private ICompanyProfileService companyProfileService;
    private ObjectionRepository objectionRepository;
    private ProcessingStatusManager statusManager;
    private ApiLogger apiLogger;

    @Autowired
    public ObjectionProcessor(IEmailService emailService,
                              ICompanyProfileService companyProfileService,
                              ObjectionRepository objectionRepository,
                              ProcessingStatusManager processingStatusManager,
                              ApiLogger apiLogger) {
        this.emailService = emailService;
        this.companyProfileService = companyProfileService;
        this.objectionRepository = objectionRepository;
        this.statusManager = processingStatusManager;
        this.apiLogger = apiLogger;
    }

    /**
     * Process the specified Objection
     * Only processes if status is SUBMITTED
     *
     * @param objection     the objection to process
     * @param httpRequestId http request id used for logging
     * @throws InvalidObjectionStatusException if the Objection is not currently in status SUBMITTED when this is called
     */
    public void process(Objection objection, String httpRequestId)
            throws InvalidObjectionStatusException, ServiceException {

        verifyCanStartProcessing(objection, httpRequestId);

        Map<String, Object> logMap = new HashMap<>();
        logMap.put(LOG_OBJECTION_ID_KEY, objection.getId());
        apiLogger.debugContext(httpRequestId, "Starting objection processing", logMap);

        if (statusManager.canProcessChips(objection.getStatus())) {
            // TODO OBJ-139/OBJ-20 do chips sending
        }

        CompanyProfileApi companyProfile = getCompanyProfile(objection, httpRequestId);

        if (statusManager.canSendInternalEmail(objection.getStatus())) {
            sendInternalEmail(objection, companyProfile, httpRequestId);
        }

        if (statusManager.canSendExternalEmail(objection.getStatus())) {
            sendExternalEmail(objection, companyProfile, httpRequestId);
        }

        setStatus(ObjectionStatus.PROCESSED, objection, httpRequestId);
    }

    private void verifyCanStartProcessing(Objection objection, String httpRequestId)
            throws InvalidObjectionStatusException {

        if (objection == null) {
            throw new IllegalArgumentException(
                    "Objection arg missing from ObjectionProcessor.verifyCanStartProcessing(Objection, String)");
        }
        if (httpRequestId == null) {
            throw new IllegalArgumentException(
                    "httpRequestId arg missing from ObjectionProcessor.verifyCanStartProcessing(Objection, String)");
        }

        // if status not processable, throw exception
        if (objection.getStatus() == null || !objection.getStatus().isProcessableStatus()) {
            InvalidObjectionStatusException statusException = new InvalidObjectionStatusException(
                    String.format(INVALID_START_STATUS_MSG, objection.getId(), objection.getStatus()));

            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LOG_OBJECTION_ID_KEY, objection.getId());
            apiLogger.errorContext(httpRequestId, statusException.getMessage(), statusException, logMap);

            throw statusException;
        }
    }

    private CompanyProfileApi getCompanyProfile(Objection objection, String httpRequestId) throws ServiceException {
        return this.companyProfileService.getCompanyProfile(objection.getCompanyNumber(), httpRequestId);
    }

    private void sendInternalEmail(Objection objection, CompanyProfileApi companyProfile,
                                   String httpRequestId) throws ServiceException {
        try {
            emailService.sendObjectionSubmittedDissolutionTeamEmail(
                    companyProfile.getCompanyName(),
                    companyProfile.getJurisdiction(),
                    objection,
                    httpRequestId);
        } catch (Exception e) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LOG_OBJECTION_ID_KEY, objection.getId());
            apiLogger.errorContext(httpRequestId, "Error sending dissolution team email", e,
                    logMap);

            setStatus(ObjectionStatus.ERROR_INT_EMAIL, objection, httpRequestId);
            throw e;
        }
    }

    private void sendExternalEmail(Objection objection, CompanyProfileApi companyProfile,
                                   String httpRequestId) throws ServiceException {
        try {
            emailService.sendObjectionSubmittedCustomerEmail(objection, companyProfile.getCompanyName(), httpRequestId);
        } catch (Exception e) {
            Map<String, Object> logMap = new HashMap<>();
            logMap.put(LOG_OBJECTION_ID_KEY, objection.getId());
            apiLogger.errorContext(httpRequestId, "Error sending customer email", e, logMap);

            setStatus(ObjectionStatus.ERROR_EXT_EMAIL, objection, httpRequestId);
            throw e;
        }
    }

    private void setStatus(ObjectionStatus newStatus, Objection objection, String httpRequestId) {
        // TODO debug log statement for status change
        objection.setStatus(newStatus);
        objectionRepository.save(objection);
    }
}
