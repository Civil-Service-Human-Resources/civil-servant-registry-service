package uk.gov.cshr.civilservant.repository;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.civilservant.domain.Domain;

import java.util.Optional;

@Repository
@RepositoryRestResource
public interface DomainRepository extends CrudRepository<Domain, Long> {
    Optional<Domain> findDomainByDomain(String domain);

}
