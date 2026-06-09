package uk.gov.cshr.civilservant.dto;

import lombok.Data;

import java.util.List;

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

  public CivilServantDto() {}
}
