package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import uk.gov.companieshouse.api.strikeoffobjections.service.IIdGeneratorService;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.function.Supplier;

@Service
public class IdGeneratorService implements IIdGeneratorService {

    private static final String ID_PREFIX = "OBJ-";

    private final Supplier<LocalDateTime> dateTimeSupplier;

    @Autowired
    public IdGeneratorService(Supplier<LocalDateTime> dateTimeSupplier) {
        this.dateTimeSupplier = dateTimeSupplier;
    }

    @Override
    public String generateId() {
        LocalDate dateNow = dateTimeSupplier.get().toLocalDate();
        String now = Long.toHexString(dateNow.toEpochDay());
        String uuid = now + UUID.randomUUID().toString();
        String formattedId = formatId(uuid);
        return ID_PREFIX + formattedId;
    }

    private String formatId(String id) {
        id = id.replace("-", "");
        id = id.substring(0, 12);
        id = id.toUpperCase();

        char separator = '-';

        StringBuilder sb = new StringBuilder(id);

        for(int i = 0; i < id.length() / 4 -1; i++) {
            sb.insert(((i + 1) * 4) + i, separator);
        }

        return sb.toString();
    }
}
