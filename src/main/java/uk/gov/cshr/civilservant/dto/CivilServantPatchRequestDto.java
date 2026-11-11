package uk.gov.cshr.civilservant.dto;

import lombok.Data;

import java.util.Set;

@Data
public class CivilServantPatchRequestDto {
    private String fullName;
    private String grade;
    private String profession;
    private Set<String> interests;
    private Set<String> otherAreasOfWork;
}
