package uk.gov.cshr.civilservant.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.civilservant.domain.Grade;

@Repository
@RepositoryRestResource
public interface GradeRepository extends CrudRepository<Grade, Long> {

  Iterable<Grade> findByOrganisationalUnitIsNull();
}
