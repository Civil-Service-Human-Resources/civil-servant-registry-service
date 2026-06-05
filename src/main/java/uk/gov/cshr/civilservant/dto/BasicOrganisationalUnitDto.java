package uk.gov.cshr.civilservant.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public class BasicOrganisationalUnitDto {
    private final Long id;
    private final Long parentId;
    private final String name;
}
