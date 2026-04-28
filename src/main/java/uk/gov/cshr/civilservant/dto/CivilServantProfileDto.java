package uk.gov.cshr.civilservant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collection;

@AllArgsConstructor
@Data
public class CivilServantProfileDto {
    private String fullName;
    private String email;
    private String uid;
    private GradeDto grade;
    private ProfessionDto profession;
    private OrganisationalUnitDto organisationalUnit;
    private Collection<ProfessionDto> otherAreasOfWork;
    private Collection<InterestDto> interests;
    private String lineManagerEmail;
    private String lineManagerName;
}
