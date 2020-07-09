package uk.gov.companieshouse.api.strikeoffobjections.controller;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;

import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Attachment;
import uk.gov.companieshouse.api.strikeoffobjections.model.response.AttachmentResponseDTO;

@Component
@Mapper(componentModel = "spring")
public interface AttachmentMapper {

    AttachmentResponseDTO attachmentEntityToAttachmentResponseDTO(Attachment attachment);
}
