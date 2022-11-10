package uk.gov.cshr.civilservant.controller.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.cshr.civilservant.dto.OrganisationalUnitDto;

import java.util.List;

@Data
@AllArgsConstructor
public class OrganisationalUnitResponse {
    List<OrganisationalUnitDto> organisationalUnits;
}
