package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import java.util.UUID;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.strikeoffobjections.service.IReferenceNumberGeneratorService;

@Service
public class ReferenceNumberGeneratorService implements IReferenceNumberGeneratorService {

    private static final String REFERENCE_NUMBER_PREFIX = "OBJ-";

    /**
     * Generates a 12 digit reference number based on a random uuid, and inserts a hyphen every 4
     * characters e.g. OBJ-1F3C-A2E4-5D6B
     *
     * @return a 12 digit reference with hyphens every 4 digits, prefixed with
     *     {@value #REFERENCE_NUMBER_PREFIX}
     */
    @Override
    public String generateReferenceNumber() {
        var unformattedReference = UUID.randomUUID().toString();
        var formattedReference = formatReference(unformattedReference);
        return REFERENCE_NUMBER_PREFIX + formattedReference;
    }

    private String formatReference(String unformattedReference) {
        unformattedReference = unformattedReference.replace("-", "");
        unformattedReference = unformattedReference.substring(0, 12);
        unformattedReference = unformattedReference.toUpperCase();

        var separator = '-';

        var sb = new StringBuilder(unformattedReference);

        var loopLength = (unformattedReference.length() / 4) - 1;
        for (var i = 0; i < loopLength; i++) {
            sb.insert(((i + 1) * 4) + i, separator);
        }

        return sb.toString();
    }
}
