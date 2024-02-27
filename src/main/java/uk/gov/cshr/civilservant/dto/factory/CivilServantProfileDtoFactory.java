package uk.gov.cshr.civilservant.dto.factory;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import uk.gov.cshr.civilservant.domain.CivilServant;
import uk.gov.cshr.civilservant.domain.Grade;
import uk.gov.cshr.civilservant.dto.CivilServantProfileDto;
import uk.gov.cshr.civilservant.dto.GradeDto;
import uk.gov.cshr.civilservant.dto.OrganisationalUnitDto;
import uk.gov.cshr.civilservant.dto.ProfessionDto;
import uk.gov.cshr.civilservant.service.identity.IdentityDTO;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
@Component
public class CivilServantProfileDtoFactory {

    private final ProfessionDtoFactory professionDtoFactory;
    private final OrganisationalUnitDtoFactory organisationalUnitDtoFactory;

    public CivilServantProfileDto create(CivilServant civilServant, IdentityDTO identityDTO) {
        GradeDto gradeDto = getGradeDto(civilServant);
        ProfessionDto professionDto = civilServant.getProfession().isPresent() ? professionDtoFactory.createSimple(civilServant.getProfession().get()) : null;
        OrganisationalUnitDto organisationalUnitDto = civilServant.getOrganisationalUnit().isPresent() ?
                organisationalUnitDtoFactory.create(civilServant.getOrganisationalUnit().get(), true, false, false) : null;
        List<String> hierarchyCodes = new ArrayList<>();
        if (organisationalUnitDto != null) {
            hierarchyCodes.add(organisationalUnitDto.getCode());
            OrganisationalUnitDto currentOrg = organisationalUnitDto;
            while (currentOrg.getParent() != null) {
                hierarchyCodes.add(currentOrg.getParent().getCode());
                currentOrg = currentOrg.getParent();
            }
        }
        return new CivilServantProfileDto(civilServant.getFullName(), identityDTO.getUsername(), identityDTO.getUid(),
                gradeDto, professionDto, organisationalUnitDto, hierarchyCodes);
    }

    private GradeDto getGradeDto(CivilServant civilServant) {
        GradeDto gradeDto = null;
        Optional<Grade> gradeOptional = civilServant.getGrade();
        if (gradeOptional.isPresent()) {
            Grade grade = gradeOptional.get();
            gradeDto = new GradeDto(grade.getId(), grade.getCode(), grade.getName());
        }
        return gradeDto;
    }

}
