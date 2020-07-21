package uk.gov.companieshouse.api.strikeoffobjections.controller;

import org.mapstruct.Mapper;
import org.springframework.stereotype.Component;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;
import uk.gov.companieshouse.api.strikeoffobjections.model.response.ObjectionResponseDTO;

@Component
@Mapper(componentModel = "spring")
public interface ObjectionMapper {

    ObjectionResponseDTO objectionEntityToObjectionResponseDTO(Objection objection);
}
