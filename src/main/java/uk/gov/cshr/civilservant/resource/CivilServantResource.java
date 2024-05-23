package uk.gov.cshr.civilservant.resource;

import lombok.Data;
import uk.gov.cshr.civilservant.domain.Identity;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.dto.GradeDto;
import uk.gov.cshr.civilservant.dto.InterestDto;
import uk.gov.cshr.civilservant.dto.ProfessionDto;

import java.util.Set;

@Data
public class CivilServantResource {
  private String fullName;
  private GradeDto grade;
  private OrganisationalUnit organisationalUnit;
  private ProfessionDto profession;
  private Set<ProfessionDto> otherAreasOfWork;
  private Set<InterestDto> interests;
  private String lineManagerName;
  private String lineManagerEmailAddress;
  private Long userId;
  private Identity identity;
}
