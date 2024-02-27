package uk.gov.cshr.civilservant.resource;

import lombok.Data;
import uk.gov.cshr.civilservant.domain.Identity;
import uk.gov.cshr.civilservant.domain.Interest;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.domain.Profession;
import uk.gov.cshr.civilservant.dto.GradeDto;

import java.util.Set;

@Data
public class CivilServantResource {
  private String fullName;
  private GradeDto grade;
  private OrganisationalUnit organisationalUnit;
  private Profession profession;
  private Set<Profession> otherAreasOfWork;
  private Set<Interest> interests;
  private String lineManagerName;
  private String lineManagerEmailAddress;
  private Long userId;
  private Identity identity;
}
