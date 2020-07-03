package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

import uk.gov.companieshouse.service.links.CoreLinkKeys;
import uk.gov.companieshouse.service.links.LinkKey;

public enum ObjectionsLinkKeys implements LinkKey {

    DOWNLOAD("download"),
    SELF(CoreLinkKeys.SELF.key());

    private final String key;

    private ObjectionsLinkKeys(String key) {
        this.key = key;
    }

    public String key() {
        return this.key;
    }
}
