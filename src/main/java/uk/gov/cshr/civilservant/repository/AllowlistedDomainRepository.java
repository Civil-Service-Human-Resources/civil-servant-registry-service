package uk.gov.cshr.civilservant.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.civilservant.domain.AllAllowlistedDomainDetails;
import uk.gov.cshr.civilservant.domain.AllowlistedDomain;

@Repository
@RepositoryRestResource(excerptProjection = AllAllowlistedDomainDetails.class)
public interface AllowlistedDomainRepository extends JpaRepository<AllowlistedDomain, Long> {

}
