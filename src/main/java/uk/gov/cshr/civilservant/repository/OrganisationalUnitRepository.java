package uk.gov.cshr.civilservant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.civilservant.domain.AgencyToken;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;

import java.util.List;
import java.util.Optional;

@Repository
@RepositoryRestResource
public interface OrganisationalUnitRepository extends SelfReferencingEntityRepository<OrganisationalUnit> {

    Optional<OrganisationalUnit> findByCode(@Param("code") String code);

    @Query(value = "select new uk.gov.cshr.civilservant.domain.OrganisationalUnit(o.name, o.code, o.abbreviation) " +
            "from OrganisationalUnit o")
    List<OrganisationalUnit> findAllNormalised();

    @Override
    <S extends OrganisationalUnit> S save(S entity);

    @Override
    @PreAuthorize("isAuthenticated()")
    void deleteById(Long aLong);

    @Query(value = "select o.code from organisational_unit o", nativeQuery = true)
    List<String> findAllCodes();

    @Query("select ou " +
        "from OrganisationalUnit ou " +
        "where ou.agencyToken = ?1 ")
    Optional<OrganisationalUnit> findOrganisationByAgencyToken(AgencyToken agencyToken);

    @Override
    List<OrganisationalUnit> findAll();

    Page<OrganisationalUnit> findAllByIdIn(Iterable<Long> ids, Pageable pageable);
}
