package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.strikeoffobjections.service.IReferenceNumberGeneratorService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class ReferenceNumberGeneratorService implements IReferenceNumberGeneratorService {

    private static final String REFERENCE_NUMBER_PREFIX = "OBJ-";

    private final Supplier<LocalDateTime> dateTimeSupplier;

    @Autowired
    public ReferenceNumberGeneratorService(Supplier<LocalDateTime> dateTimeSupplier) {
        this.dateTimeSupplier = dateTimeSupplier;
    }

    /**
     * Generates a 12 digit reference number based on the day and a random uuid, and inserts a hyphen every 4 characters
     * e.g. OBJ-1F3C-A2E4-5D6B
     * @return a 12 digit reference with hyphens every 4 digits, prefixed with REFERENCE_NUMBER_PREFIX
     */
    @Override
    public String generateReferenceNumber() {
        LocalDate dateNow = dateTimeSupplier.get().toLocalDate();
        String now = Long.toHexString(dateNow.toEpochDay());
        String unformattedReference = now + UUID.randomUUID().toString();
        String formattedReference = formatReference(unformattedReference);
        return REFERENCE_NUMBER_PREFIX + formattedReference;
    }

    private String formatReference(String unformattedReference) {
        unformattedReference = unformattedReference.replace("-", "");
        unformattedReference = unformattedReference.substring(0, 12);
        unformattedReference = unformattedReference.toUpperCase();

        char separator = '-';

        StringBuilder sb = new StringBuilder(unformattedReference);

        for(int i = 0; i < unformattedReference.length() / 4 -1; i++) {
            sb.insert(((i + 1) * 4) + i, separator);
        }

        return sb.toString();
    }
}
