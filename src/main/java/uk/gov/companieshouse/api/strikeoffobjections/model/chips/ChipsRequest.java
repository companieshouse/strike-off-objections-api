package uk.gov.companieshouse.api.strikeoffobjections.model.chips;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang.StringUtils;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.ObjectionLinkKeys;
import uk.gov.companieshouse.service.links.Links;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class ChipsRequest {

    @JsonProperty("objection_id")
    private String objectionId;

    @JsonProperty("company_number")
    private String companyNumber;

    @JsonProperty("attachments")
    private Map<String, String> attachments;

    @JsonProperty("reference_number")
    private String referenceNumber;

    @JsonProperty("full_name")
    private String fullName;

    @JsonProperty("share_identity")
    private Boolean shareIdentity;

    @JsonProperty("customer_email")
    private String customerEmail;

    @JsonProperty("reason")
    private String reason;

    private ChipsRequest() {
        // intentionally blank
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

    public String getFullName() {
        return fullName;
    }

    public Boolean isShareIdentity() {
        return shareIdentity;
    }

    public String getCustomerEmail() {
        return customerEmail;
    }

    public String getReason() {
        return reason;
    }

    @Override
    public String toString() {
        return "ChipsRequest{"
                + "objectionId='"
                + objectionId
                + '\''
                + ",companyNumber='"
                + companyNumber
                + '\''
                + ",attachments='"
                + getAttachmentsAsString()
                + '\''
                + ",referenceNumber='"
                + referenceNumber
                + '\''
                + ",fullName='"
                + fullName
                + '\''
                + ",shareIdentity='"
                + shareIdentity
                + '\''
                + ",customerEmail='"
                + customerEmail
                + '\''
                + ",reason='"
                + reason
                + '\''
                + "}";
    }

    private String getAttachmentsAsString() {
        return StringUtils.join(Collections.singleton(attachments), ',');
    }

    public static class Builder {
        private String objectionId;
        private String companyNumber;
        private List<Attachment> attachments;
        private String referenceNumber;
        private String fullName;
        private Boolean shareIdentity;
        private String customerEmail;
        private String reason;
        private String downloadPrefix;

        public ChipsRequest build() {
            var chipsRequest = new ChipsRequest();
            chipsRequest.objectionId = this.objectionId;
            chipsRequest.companyNumber = this.companyNumber;
            chipsRequest.attachments = buildAttachmentsMap(downloadPrefix, attachments);
            chipsRequest.referenceNumber = this.referenceNumber;
            chipsRequest.fullName = this.fullName;
            chipsRequest.shareIdentity = this.shareIdentity;
            chipsRequest.customerEmail = this.customerEmail;
            chipsRequest.reason = this.reason;
            return chipsRequest;
        }

        public Builder objectionId(String objectionId) {
            this.objectionId = objectionId;
            return this;
        }

        public Builder companyNumber(String companyNumber) {
            this.companyNumber = companyNumber;
            return this;
        }

        public Builder attachments(String downloadPrefixm, List<Attachment> attachments) {
            this.downloadPrefix = downloadPrefixm;
            this.attachments = attachments;
            return this;
        }

        public Builder referenceNumber(String referenceNumber) {
            this.referenceNumber = referenceNumber;
            return this;
        }

        public Builder fullName(String fullName) {
            this.fullName = fullName;
            return this;
        }

        public Builder shareIdentity(Boolean shareIdentity) {
            this.shareIdentity = shareIdentity;
            return this;
        }

        public Builder customerEmail(String customerEmail) {
            this.customerEmail = customerEmail;
            return this;
        }

        public Builder reason(String reason) {
            this.reason = reason;
            return this;
        }

        private Map<String, String> buildAttachmentsMap(
                String downloadPrefix, List<Attachment> attachments) {
            if (attachments == null) {
                return null;
            }

            Map<String, String> attachmentsMap = new HashMap<>();
            for (Attachment attachment : attachments) {
                String name = attachment.getName();
                Links links = attachment.getLinks();
                if (links != null) {
                    String downloadLink =
                            String.format("%s%s", downloadPrefix, links.getLink(ObjectionLinkKeys.DOWNLOAD));
                    attachmentsMap.put(name, downloadLink);
                }
            }
            return Collections.unmodifiableMap(attachmentsMap);
        }
    }
}
