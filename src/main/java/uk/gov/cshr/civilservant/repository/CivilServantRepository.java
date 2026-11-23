package uk.gov.cshr.civilservant.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.rest.core.annotation.RepositoryRestResource;
import org.springframework.data.rest.core.annotation.RestResource;
import org.springframework.security.access.prepost.PostAuthorize;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Repository;
import uk.gov.cshr.civilservant.domain.*;
import uk.gov.cshr.civilservant.dto.CivilServantReportDto;

import java.util.List;
import java.util.Optional;

@Repository
@PreAuthorize("isAuthenticated()")
@RepositoryRestResource(excerptProjection = AllCivilServantDetails.class)
public interface CivilServantRepository extends JpaRepository<CivilServant, Long> {

  @RestResource(exported = false)
  @Query(
      "select c from CivilServant c "
          + "LEFT JOIN FETCH c.organisationalUnit "
          + "LEFT JOIN FETCH c.profession "
          + "LEFT JOIN FETCH c.lineManager "
          + "LEFT JOIN FETCH c.grade "
          + "LEFT JOIN FETCH c.otherAreasOfWork "
          + "LEFT JOIN FETCH c.otherOrganisationalUnits "
          + "LEFT JOIN FETCH c.interests "
          + "WHERE c.identity.uid = ?#{authentication.name}")
  Optional<CivilServant> findByPrincipal();

  @PreAuthorize("#civilServant.identity.uid == authentication.name || hasAnyAuthority('INTERNAL','IDENTITY_MANAGE_IDENTITY')")
  CivilServant save(@Param("civilServant") CivilServant civilServant);

  @PostAuthorize("returnObject.isPresent() && (returnObject.get().identity.uid == authentication.name || hasAnyAuthority('INTERNAL','IDENTITY_MANAGE_IDENTITY'))")
  @Query(
      "select c from CivilServant c "
          + "LEFT JOIN FETCH c.organisationalUnit "
          + "LEFT JOIN FETCH c.profession "
          + "LEFT JOIN FETCH c.lineManager "
          + "LEFT JOIN FETCH c.grade "
          + "LEFT JOIN FETCH c.otherAreasOfWork "
          + "LEFT JOIN FETCH c.otherOrganisationalUnits "
          + "LEFT JOIN FETCH c.interests "
          + "WHERE c.id = ?1")
  Optional<CivilServant> findById(@Param("id") Long id);

  @RestResource(path = "findByIdentity", rel = "findByIdentity")
  @Query("select c from CivilServant c where c.identity.uid = ?1")
  Optional<CivilServant> findByIdentity(@Param("uid") String uid);

  @RestResource(exported = false)
  Optional<CivilServant> findByIdentity(Identity identity);

  List<CivilServant> findAllByOrganisationalUnit(OrganisationalUnit organisationalUnit);

  List<CivilServant> findAllByProfession(Profession profession);

  @Query("select c.identity from CivilServant c " +
      "WHERE c.organisationalUnit.code = ?1")
  Page<Identity> findAllIdentitiesByOrganisationCode(Pageable pageable, String organisationalUnitCode);

  @Query(
          value = "select cs.id, cs.full_name, ou.name, p.name, i.uid, g.name, GROUP_CONCAT(p2.name) "
          + "from civil_servant c "
          + "left join organisational_unit ou on ou.id = cs.organisational_unit_id "
          + "left join profession p on p.id = cs.profession_id "
          + "left join identity i on i.id = cs.identity_id "
          + "left join grade g on g.id = cs.grade_id "
          + "left join civil_servant_other_areas_of_work csoaow on csoaow.civil_servant_id = cs.id "
          + "left join profession p2 on p2.id = csoaow.other_areas_of_work_id "
          + "group by cs.id",
          nativeQuery = true
  )
  List<CivilServantReportDto> findAllNormalised();

  @Query(
          value = "select cs.id, cs.full_name, ou.code, ou.id, ou.name, p.name, i.uid, g.name, GROUP_CONCAT(p2.name) " +
                  "from civil_servant cs " +
                  "left join organisational_unit ou on ou.id = cs.organisational_unit_id " +
                  "left join profession p on p.id = cs.profession_id " +
                  "left join identity i on i.id = cs.identity_id " +
                  "left join grade g on g.id = cs.grade_id " +
                  "left join civil_servant_other_areas_of_work csoaow on csoaow.civil_servant_id = cs.id " +
                  "left join profession p2 on p2.id = csoaow.other_areas_of_work_id " +
                  "where i.uid in ?1 and ou.id in ?2 " +
                  "group by cs.id",
          nativeQuery = true
  )
  List<CivilServantReportDto> findAllByUidsNormalisedWithOrgs(List<String> uids, List<Long> organisationalUnits);

  @Query(
          value = "select CAST(cs.id AS BIGINT), cs.full_name, ou.code, CAST(ou.id AS BIGINT), ou.name, p.name, i.uid, g.name, GROUP_CONCAT(p2.name) " +
                  "from civil_servant cs " +
                  "left join organisational_unit ou on ou.id = cs.organisational_unit_id " +
                  "left join profession p on p.id = cs.profession_id " +
                  "left join identity i on i.id = cs.identity_id " +
                  "left join grade g on g.id = cs.grade_id " +
                  "left join civil_servant_other_areas_of_work csoaow on csoaow.civil_servant_id = cs.id " +
                  "left join profession p2 on p2.id = csoaow.other_areas_of_work_id " +
                  "where i.uid in ?1 " +
                  "group by cs.id",
          nativeQuery = true
  )
  List<CivilServantReportDto> findAllByUidsNormalised(List<String> uids);

  default List<CivilServantReportDto> findAllByUidsNormalised(List<String> uids, List<Long> organisationalUnits) {
    if (organisationalUnits != null) {
      return this.findAllByUidsNormalisedWithOrgs(uids, organisationalUnits);
    }
    return this.findAllByUidsNormalised(uids);
  }

  @Query(
          value = "select cs.id, cs.full_name, ou.name, p.name, i.uid, g.name, GROUP_CONCAT(p2.name) " +
                  "from civil_servant cs " +
                  "left join organisational_unit ou on ou.id = cs.organisational_unit_id " +
                  "left join profession p on p.id = cs.profession_id " +
                  "left join identity i on i.id = cs.identity_id " +
                  "left join grade g on g.id = cs.grade_id " +
                  "left join civil_servant_other_areas_of_work csoaow on csoaow.civil_servant_id = cs.id " +
                  "left join profession p2 on p2.id = csoaow.other_areas_of_work_id " +
                  "where cs.organisational_unit_id = ?1 " +
                  "group by cs.id",
          nativeQuery = true
  )
  List<CivilServantReportDto> findAllByOrganisationNormalised(OrganisationalUnit organisationalUnit);

  @Query(
      "select new uk.gov.cshr.civilservant.dto.CivilServantReportDto(c.fullName, ou.code, p.name, i.uid, g.name, li.uid) "
          + "from CivilServant c "
          + "left join OrganisationalUnit ou on ou.id = c.organisationalUnit.id "
          + "left join Profession p on p.id = c.profession.id "
          + "left join Identity i on i.id = c.identity.id "
          + "left join Grade g on g.id = c.grade.id "
          + "left join Identity li on li.id = c.lineManager.identity.id "
          + "group by c.id ")
  List<CivilServantReportDto> findAllNormalisedWithCodes();

  @Query(
      "select new uk.gov.cshr.civilservant.dto.CivilServantReportDto(c.fullName, ou.code, p.name, i.uid) "
          + "from CivilServant c "
          + "left join OrganisationalUnit ou on ou.id = c.organisationalUnit.id "
          + "left join Profession p on p.id = c.profession.id "
          + "left join Identity i on i.id = c.identity.id "
          + "where ou.code = ?1 "
          + "group by c.id ")
  List<CivilServantReportDto> findAllByOrganisationCodeNormalised(String organisationalUnitCode);
}
