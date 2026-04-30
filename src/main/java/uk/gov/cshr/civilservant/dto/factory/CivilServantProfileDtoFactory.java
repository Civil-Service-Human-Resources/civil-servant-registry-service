package uk.gov.cshr.civilservant.dto.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.cshr.civilservant.domain.CivilServant;
import uk.gov.cshr.civilservant.domain.Grade;
import uk.gov.cshr.civilservant.dto.*;
import uk.gov.cshr.civilservant.exception.CivilServantNotFoundException;
import uk.gov.cshr.civilservant.repository.CivilServantRepository;
import uk.gov.cshr.civilservant.service.identity.IdentityDTO;
import uk.gov.cshr.civilservant.service.identity.IdentityService;

import java.util.Collection;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Component
public class CivilServantProfileDtoFactory {

    private final ProfessionDtoFactory professionDtoFactory;
    private final OrganisationalUnitDtoFactory organisationalUnitDtoFactory;
    private final InterestDtoFactory interestDtoFactory;
    private final IdentityService identityService;
    private final CivilServantRepository civilServantRepository;

    public CivilServantProfileDto create(String uid) {
        IdentityDTO identityDTO = identityService.getidentity(uid);
        CivilServant civilServant = civilServantRepository.findByIdentity(uid).orElseThrow(CivilServantNotFoundException::new);
        String lineManagerName = null;
        String lineManagerEmail = null;
        CivilServant lineManager = civilServant.getLineManager().orElse(null);
        if (lineManager != null) {
            IdentityDTO lmIdentity = identityService.getidentity(lineManager.getIdentity().getUid());
            lineManagerEmail = lmIdentity != null ? lmIdentity.getUsername() : null;
            lineManagerName = lineManager.getFullName();
        }
        GradeDto gradeDto = getGradeDto(civilServant);
        ProfessionDto professionDto = civilServant.getProfession().isPresent() ? professionDtoFactory.createSimple(civilServant.getProfession().get()) : null;
        Collection<ProfessionDto> otherAreasOfWork = civilServant.getOtherAreasOfWork().stream().map(professionDtoFactory::createSimple).collect(Collectors.toList());
        Collection<InterestDto> interests = civilServant.getInterests().stream().map(interestDtoFactory::createSimple).collect(Collectors.toList());
        OrganisationalUnitDto organisationalUnitDto = civilServant.getOrganisationalUnit().isPresent() ?
                organisationalUnitDtoFactory.create(civilServant.getOrganisationalUnit().get(), true, false, false) : null;
        return new CivilServantProfileDto(civilServant.getFullName(), identityDTO.getUsername(), identityDTO.getUid(),
                gradeDto, professionDto, organisationalUnitDto, otherAreasOfWork, interests, lineManagerEmail, lineManagerName);
    }

    private GradeDto getGradeDto(CivilServant civilServant) {
        GradeDto gradeDto = null;
        Grade grade = civilServant.getGrade();
        if (grade != null) {
            gradeDto = new GradeDto(grade.getId(), grade.getCode(), grade.getName());
        }
        return gradeDto;
    }

}
