package uk.gov.cshr.civilservant.dto;

import java.util.List;

import lombok.Data;

@Data
public class CivilServantDto {
  private String id;
  private String uid;
  private String name;
  private String email;
  private String organisation;
  private String profession;
  private List<String> otherAreasOfWork;
  private List<String> otherOrganisationalUnits;
  private String grade;

  public CivilServantDto(
      Long id, String name, String organisation, String profession, String uid, String grade) {
    this.id = id.toString();
    this.name = name;
    this.organisation = organisation;
    this.profession = profession;
    this.uid = uid;
    this.grade = grade;
  }

  public CivilServantDto() {}
}
