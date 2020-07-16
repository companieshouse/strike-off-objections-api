package uk.gov.companieshouse.api.strikeoffobjections.model.entity;

import org.springframework.data.mongodb.core.mapping.Field;
import uk.gov.companieshouse.service.links.Links;

import java.io.Serializable;
import java.util.Objects;

public class Attachment implements Serializable {

    @Field("id")
    private String id;
    @Field("links")
    private Links links;
    @Field("name")
    private String name;
    @Field("content_type")
    private String contentType;
    @Field("size")
    private long size;

    public Links getLinks() {
        return links;
    }

    public void setLinks(Links links) {
        this.links = links;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Attachment that = (Attachment) o;
        return getSize() == that.getSize() &&
                Objects.equals(getId(), that.getId()) &&
                Objects.equals(getLinks(), that.getLinks()) &&
                Objects.equals(getName(), that.getName()) &&
                Objects.equals(getContentType(), that.getContentType());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getId(), getLinks(), getName(), getContentType(), getSize());
    }
}
