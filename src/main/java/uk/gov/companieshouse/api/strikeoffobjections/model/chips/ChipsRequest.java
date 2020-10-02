package uk.gov.companieshouse.api.strikeoffobjections.model.chips;

import com.fasterxml.jackson.annotation.JsonProperty;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.service.links.CoreLinkKeys;
import uk.gov.companieshouse.service.links.Links;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChipsRequest {

    @JsonProperty("objection_id")
    private final String objectionId;

    @JsonProperty("company_number")
    private final String companyNumber;

    @JsonProperty("attachments")
    private final Map<String, String> attachments;

    @JsonProperty("reference_number")
    private final String referenceNumber;

    @JsonProperty("customer_email")
    private final String customerEmail;

    @JsonProperty("reason")
    private final String reason;

    public ChipsRequest(String objectionId, String companyNumber,
                        List<Attachment> attachments, String referenceNumber,
                        String customerEmail, String reason,
                        String downloadPrefix) {
        this.objectionId = objectionId;
        this.companyNumber = companyNumber;
        this.attachments = buildAttachmentsMap(downloadPrefix, attachments);
        this.referenceNumber = referenceNumber;
        this.customerEmail = customerEmail;
        this.reason = reason;
    }

    public String getObjectionId() {
        return objectionId;
    }

    public String getCompanyNumber() {
        return companyNumber;
    }

    public Map<String, String> getAttachments() {
        return attachments;
    }

    public String getReferenceNumber() {
        return referenceNumber;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getReason() {
        return reason;
    }

    private Map<String, String> buildAttachmentsMap(String downloadPrefix,
                                                    List<Attachment> attachments) {
        Map<String, String> attachmentsMap = new HashMap<>();
        for (Attachment attachment : attachments) {
            String name = attachment.getName();
            Links links = attachment.getLinks();
            if(links != null) {
                String downloadLink = String.format("%s%s", downloadPrefix,
                        links.getLink(CoreLinkKeys.SELF));
                attachmentsMap.put(name, downloadLink);
            }
        }
        return attachmentsMap;
    }

    @Override
    public String toString() {
        return "ChipsRequest{" +
            "objectionId='" + objectionId + '\'' +
            ",companyNumber='" + companyNumber + '\'' +
            ",attachments='" + attachments.values() + '\'' +
            ",referenceNumber='" + referenceNumber + '\'' +
            ",customerEmail='" + customerEmail + '\'' +
            ",reason='" + reason + '\'' +
            "}";
    }
}
