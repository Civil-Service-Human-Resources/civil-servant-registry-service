package uk.gov.cshr.civilservant.dto;

import lombok.Data;

@Data
public class DtoEntity {
    protected String name;
    protected String href;
    protected String abbreviation;
    protected String formattedName;
}