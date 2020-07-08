package uk.gov.companieshouse.api.strikeoffobjections.model.response;

import uk.gov.companieshouse.service.links.Links;

public class AttachmentResponseDTO {

    private String id;
    
    private Links links;
    
    private String name;
    
    private String contentType;
    
    private long size;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }
}
