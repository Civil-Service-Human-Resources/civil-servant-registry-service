package uk.gov.cshr.civilservant.dto.factory;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.dto.OrganisationalUnitDto;
import uk.gov.cshr.civilservant.service.RepositoryEntityService;

@Component
@Slf4j
public class OrganisationalUnitDtoFactory
    extends DtoFactory<OrganisationalUnitDto, OrganisationalUnit> {

  private final RepositoryEntityService<OrganisationalUnit> repositoryEntityService;

  public OrganisationalUnitDtoFactory(RepositoryEntityService repositoryEntityService) {
    this.repositoryEntityService = repositoryEntityService;
  }

  public OrganisationalUnitDto create(OrganisationalUnit organisationalUnit, boolean includeParent, boolean includeFormattedName) {
    OrganisationalUnitDto organisationalUnitDto = new OrganisationalUnitDto();
    organisationalUnitDto.setId(organisationalUnit.getId());
    organisationalUnitDto.setCode(organisationalUnit.getCode());
    organisationalUnitDto.setName(organisationalUnit.getName());
    if (includeFormattedName) {
      organisationalUnitDto.setFormattedName(formatName(organisationalUnit));
    }
    if (organisationalUnit.getParent() != null) {
      organisationalUnitDto.setParentId(organisationalUnit.getParent().getId());
      if (includeParent) {
        organisationalUnitDto.setParent(this.create(organisationalUnit.getParent(), true, false));
      }
    }
    organisationalUnitDto.setAbbreviation(organisationalUnit.getAbbreviation());
    organisationalUnitDto.setHref(repositoryEntityService.getUri(organisationalUnit));
    organisationalUnitDto.setAgencyToken(organisationalUnit.getAgencyToken());

    return organisationalUnitDto;
  }

  public OrganisationalUnitDto create(OrganisationalUnit organisationalUnit) {
    return create(organisationalUnit, false, true);
  }

  /**
   * Format the name of an organisationalUnit to be prefixed with parental hierarchy.
   *
   * <p>e.g. Cabinet Office (CO) | Child (C) | Subchild (SC)
   */
  String formatName(OrganisationalUnit organisationalUnit) {
    OrganisationalUnit currentNode = organisationalUnit;

    String name = currentNode.getName() + formatAbbreviationForNode(currentNode);

    while (currentNode.hasParent()) {
      currentNode = currentNode.getParent();

      StringBuilder sb = new StringBuilder();
      name =
          sb.append(currentNode.getName())
              .append(formatAbbreviationForNode(currentNode))
              .append(" | ")
              .append(name)
              .toString();
    }

    return name;
  }

  /**
   * If an organisational unit has an abbreviation, we should format it to be surrounded by
   * parenthesis, Otherwise, we should leave as blank
   *
   * <p>e.g: With abbreviation -> Cabinet Office (CO) | Child (C) | Subchild (SC) With no
   * abbreviation -> Cabinet Office | Child | Subchild
   */
  private String formatAbbreviationForNode(OrganisationalUnit node) {
    return (node.getAbbreviation() != null && !node.getAbbreviation().equals(""))
        ? " (" + node.getAbbreviation() + ")"
        : "";
  }
}
