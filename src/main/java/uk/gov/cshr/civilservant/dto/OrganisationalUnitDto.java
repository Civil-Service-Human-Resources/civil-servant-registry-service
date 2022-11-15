package uk.gov.cshr.civilservant.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;

@EqualsAndHashCode(callSuper = false)
@Data
public class OrganisationalUnitDto extends DtoEntity {

    private static final long serialVersionUID = 1L;

    protected String formattedName;
    private String code;
}
