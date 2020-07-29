package uk.gov.companieshouse.api.strikeoffobjections.service.impl;

import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

/**
 * Helper class to parse the authorised user information ERIC puts in the HTTP Request headers.
 */
@Component
public class ERICHeaderParser {

    private static final String DELIMITER = ";";
    private static final String EMAIL_IDENTIFIER = "@";


    public String getEmailAddress(String ericAuthorisedUser) {
        String email = null;
        if (ericAuthorisedUser != null) {
            String[] values = ericAuthorisedUser.split(DELIMITER);

            //email should be first value in the string
            String firstValue = values[0];

            if (firstValue.contains(EMAIL_IDENTIFIER)) {
                email = firstValue;
            }
        }

        return email;
    }

    public String getFullName(String ericAuthorisedUser) {
        AtomicReference<String> foreName = new AtomicReference<>();
        AtomicReference<String> surname = new AtomicReference<>();

        if (ericAuthorisedUser != null) {
            List<String> values = Arrays.asList(ericAuthorisedUser.split(DELIMITER));
            values.parallelStream().forEach(
                    v -> {
                        if (v.contains("forename=")) {
                            foreName.set(v.substring(10));
                        } else if (v.contains("surname=")) {
                            surname.set(v.substring(9));
                        }
                    }
            );
        }

        return foreName.get() + " " + surname.get();
    }
}
