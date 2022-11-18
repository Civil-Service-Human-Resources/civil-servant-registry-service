package uk.gov.cshr.civilservant.dto;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.cshr.civilservant.domain.AgencyToken;

@EqualsAndHashCode(callSuper = false)
@Data
public class OrganisationalUnitDto extends DtoEntity {

    private static final long serialVersionUID = 1L;
    private Long parentId;
    private OrganisationalUnitDto parent;

    protected String formattedName;
    private String code;
    private String abbreviation;
    private AgencyToken agencyToken;

}
