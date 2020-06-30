package uk.gov.companieshouse.api.strikeoffobjections.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.StrikeOffObjectionsEntity;

@Repository
public interface StrikeOffObjectionsRepository extends MongoRepository<StrikeOffObjectionsEntity, String> {
}
