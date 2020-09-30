package uk.gov.companieshouse.api.strikeoffobjections.controller;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.CreatedBy;
import uk.gov.companieshouse.api.strikeoffobjections.model.response.CreatedByResponseDTO;

@Component
@Mapper(componentModel = "spring")
public interface CreatedByMapper {

    CreatedByResponseDTO createdByEntityToCreatedByResponseDTO(CreatedBy createdBy);
}
