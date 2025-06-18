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
          + "WHERE c.identity.uid = ?#{principal}")
  Optional<CivilServant> findByPrincipal();

  @PreAuthorize("#civilServant.identity.uid eq principal || hasAnyAuthority('INTERNAL','IDENTITY_MANAGE_IDENTITY')")
  CivilServant save(@Param("civilServant") CivilServant civilServant);

  @PostAuthorize("returnObject.isPresent() && (returnObject.get().identity.uid eq principal || hasAnyAuthority('INTERNAL','IDENTITY_MANAGE_IDENTITY'))")
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
      "select new uk.gov.cshr.civilservant.dto.CivilServantReportDto(c.id, c.fullName, ou.name, p.name, i.uid, g.name, group_concat(oaw.name)) "
          + "from CivilServant c "
          + "left join OrganisationalUnit ou on ou.id = c.organisationalUnit.id "
          + "left join Profession p on p.id = c.profession.id "
          + "left join Identity i on i.id = c.identity.id "
          + "left join Grade g on g.id = c.grade.id "
          + "join c.otherAreasOfWork oaw "
          + "group by c.id ")
  List<CivilServantReportDto> findAllNormalised();

  @Query(
          "select new uk.gov.cshr.civilservant.dto.CivilServantReportDto(c.id, c.fullName, ou.name, p.name, i.uid, g.name, group_concat(oaw.name)) "
                  + "from CivilServant c "
                  + "left join OrganisationalUnit ou on ou.id = c.organisationalUnit.id "
                  + "left join Profession p on p.id = c.profession.id "
                  + "left join Identity i on i.id = c.identity.id "
                  + "left join Grade g on g.id = c.grade.id "
                  + "join c.otherAreasOfWork oaw "
                  + "where i.uid in (?1) "
                  + "group by c.id ")
  List<CivilServantReportDto> findAllByUidsNormalised(List<String> uids);

  @Query(
      "select new uk.gov.cshr.civilservant.dto.CivilServantReportDto(c.id, c.fullName, ou.name, p.name, i.uid, g.name, group_concat(oaw.name)) "
          + "from CivilServant c "
          + "left join OrganisationalUnit ou on ou.id = c.organisationalUnit.id "
          + "left join Profession p on p.id = c.profession.id "
          + "left join Identity i on i.id = c.identity.id "
          + "left join Grade g on g.id = c.grade.id "
          + "join c.otherAreasOfWork oaw "
          + "where ou = ?1 "
          + "group by c.id ")
  List<CivilServantReportDto> findAllByOrganisationNormalised(
      OrganisationalUnit organisationalUnit);

  @Query(
      "select new uk.gov.cshr.civilservant.dto.CivilServantReportDto(c.id, c.fullName, ou.name, p.name, i.uid, g.name, group_concat(oaw.name)) "
          + "from CivilServant c "
          + "left join OrganisationalUnit ou on ou.id = c.organisationalUnit.id "
          + "left join Profession p on p.id = c.profession.id "
          + "left join Identity i on i.id = c.identity.id "
          + "left join Grade g on g.id = c.grade.id "
          + "join c.otherAreasOfWork oaw "
          + "where p = ?1 "
          + "group by c.id ")
  List<CivilServantReportDto> findAllByProfessionNormalised(Profession profession);

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
