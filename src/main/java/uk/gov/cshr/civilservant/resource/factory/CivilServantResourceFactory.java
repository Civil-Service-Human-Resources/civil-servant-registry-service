package uk.gov.cshr.civilservant.resource.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.civilservant.domain.CivilServant;
import uk.gov.cshr.civilservant.domain.Grade;
import uk.gov.cshr.civilservant.dto.*;
import uk.gov.cshr.civilservant.dto.factory.OrganisationalUnitDtoFactory;
import uk.gov.cshr.civilservant.dto.factory.ProfessionDtoFactory;
import uk.gov.cshr.civilservant.resource.CivilServantResource;
import uk.gov.cshr.civilservant.service.identity.IdentityService;

import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

@Component
@RequiredArgsConstructor
public class CivilServantResourceFactory {
    private final IdentityService identityService;
    private final LinkFactory linkFactory;
    private final ProfessionDtoFactory ProfessionDtoFactory;
    private final OrganisationalUnitDtoFactory organisationalUnitDtoFactory;

    @Transactional
    public Resource<CivilServantResource> create(CivilServant civilServant) {

        CivilServantResource civilServantResource = new CivilServantResource();

        civilServantResource.setFullName(civilServant.getFullName());

        Grade grade = civilServant.getGrade();
        if (grade != null) {
            civilServantResource.setGrade(GradeDto.fromGrade(grade));
        }

        if (civilServant.getOrganisationalUnit().isPresent()) {
            OrganisationalUnitDto organisationalUnitDto = organisationalUnitDtoFactory.create(civilServant.getOrganisationalUnit().get(),
                    false, false, false);
            civilServantResource.setOrganisationalUnit(organisationalUnitDto);
        }

        if (civilServant.getProfession().isPresent()) {
            ProfessionDto professionDto = ProfessionDtoFactory.createSimple(civilServant.getProfession().get());
            civilServantResource.setProfession(professionDto);
        }

        if (civilServant.getLineManager().isPresent()) {
            CivilServant lineManager = civilServant.getLineManager().get();

            civilServantResource.setLineManagerName(lineManager.getFullName());
            civilServantResource.setLineManagerEmailAddress(identityService.getEmailAddress(lineManager));
        }

        civilServantResource.setUserId(civilServant.getId());

        Set<InterestDto> interests = civilServant.getInterests().stream().map(i -> new InterestDto(i.getId(), i.getName())).collect(Collectors.toSet());
        civilServantResource.setInterests(interests);

        Set<ProfessionDto> otherAreasOfWork = civilServant.getOtherAreasOfWork().stream().map(this.ProfessionDtoFactory::createSimple).collect(Collectors.toSet());
        civilServantResource.setOtherAreasOfWork(otherAreasOfWork);

        Set<OrganisationalUnitDto> otherOrganisationalUnits =
                civilServant.getOtherOrganisationalUnits()
                        .stream()
                        .map(o -> organisationalUnitDtoFactory.create(o, false, false, false))
                        .collect(Collectors.toSet());
        civilServantResource.setOtherOrganisationalUnits(otherOrganisationalUnits);

        civilServantResource.setIdentity(civilServant.getIdentity());

        Resource<CivilServantResource> resource = new Resource<>(civilServantResource);

        resource.add(linkFactory.createSelfLink(civilServant));
        resource.add(linkFactory.createRelationshipLink(civilServant, "organisationalUnit"));
        resource.add(linkFactory.createRelationshipLink(civilServant, "grade"));
        resource.add(linkFactory.createRelationshipLink(civilServant, "profession"));

        return resource;
    }

    public Optional<OrgCodeDTO> getCivilServantOrganisationalUnitCode(CivilServant civilServant) {
        if(civilServant.getOrganisationalUnit().isPresent() && civilServant.getOrganisationalUnit().get().getCode() != null){
            OrgCodeDTO dto = new OrgCodeDTO();
            dto.setCode(civilServant.getOrganisationalUnit().get().getCode());
            return Optional.of(dto);
        } else {
            return Optional.empty();
        }
    }

}
