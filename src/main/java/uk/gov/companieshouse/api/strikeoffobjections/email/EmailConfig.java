package uk.gov.companieshouse.api.strikeoffobjections.email;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class EmailConfig {

    @Value("${EMAIL_SENDER_APP_ID}")
    private String originatingAppId;

    @Value(("${EMAIL_SUBMITTED_EXTERNAL_TEMPLATE_MESSAGE_TYPE}"))
    private String submittedCustomerEmailType;

    @Value(("${EMAIL_SUBMITTED_INTERNAL_TEMPLATE_MESSAGE_TYPE}"))
    private String submittedDissolutionTeamEmailType;

    @Value(("${EMAIL_RECIPIENTS_CARDIFF}"))
    private String emailRecipientsCardiff;

    @Value(("${EMAIL_RECIPIENTS_EDINBURGH}"))
    private String emailRecipientsEdinburgh;

    @Value(("${EMAIL_RECIPIENTS_BELFAST}"))
    private String emailRecipientsBelfast;

    public String getOriginatingAppId() {
        return originatingAppId;
    }

    public String getSubmittedCustomerEmailType() {
        return submittedCustomerEmailType;
    }

    public String getSubmittedDissolutionTeamEmailType() {
        return submittedDissolutionTeamEmailType;
    }

    public String getEmailRecipientsCardiff() {
        return emailRecipientsCardiff;
    }

    public String getEmailRecipientsEdinburgh() {
        return emailRecipientsEdinburgh;
    }

    public String getEmailRecipientsBelfast() {
        return emailRecipientsBelfast;
    }
}
