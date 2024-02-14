package uk.gov.companieshouse.api.strikeoffobjections.repository;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;
import uk.gov.companieshouse.api.strikeoffobjections.model.entity.Objection;

@Repository
public interface ObjectionRepository extends MongoRepository<Objection, String> {}
