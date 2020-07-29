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
                    value -> {
                        if (value.contains("forename")) {
                            foreName.set(value.substring(value.lastIndexOf('=') + 1));
                        } else if (value.contains("surname")) {
                            surname.set(value.substring(value.lastIndexOf('=') + 1));
                        }
                    }
            );
        }

        String fullName ="";

        if (foreName.get() != null) {
            fullName = fullName.concat(foreName.get() + " ");
        }

        if(surname.get() != null) {
            fullName = fullName.concat(surname.get());
        }
        return fullName;
    }
}
