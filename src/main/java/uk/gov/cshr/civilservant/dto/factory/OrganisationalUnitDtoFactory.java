package uk.gov.cshr.civilservant.dto.factory;

import org.springframework.stereotype.Component;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.dto.OrganisationalUnitDto;
import uk.gov.cshr.civilservant.service.RepositoryEntityService;

@Component
public class OrganisationalUnitDtoFactory
    extends DtoFactory<OrganisationalUnitDto, OrganisationalUnit> {

  private RepositoryEntityService<OrganisationalUnit> repositoryEntityService;

  public OrganisationalUnitDtoFactory(RepositoryEntityService repositoryEntityService) {
    this.repositoryEntityService = repositoryEntityService;
  }

  public OrganisationalUnitDto create(OrganisationalUnit organisationalUnit, boolean includeParents, boolean formatName) {
    OrganisationalUnitDto organisationalUnitDto = new OrganisationalUnitDto();
    organisationalUnitDto.setCode(organisationalUnit.getCode());
    organisationalUnitDto.setName(organisationalUnit.getName());
    organisationalUnitDto.setAbbreviation(organisationalUnit.getAbbreviation());
    organisationalUnitDto.setId(organisationalUnit.getId());
    if (formatName) {
      organisationalUnitDto.setFormattedName(formatName(organisationalUnit));
    }
    organisationalUnitDto.setHref(repositoryEntityService.getUri(organisationalUnit));
    if (includeParents && organisationalUnit.hasParent()) {
      organisationalUnitDto.setParentId(organisationalUnit.getParentId());
      organisationalUnitDto.setParent(create(organisationalUnit.getParent(), true, false));
    }
    organisationalUnitDto.setDomains(organisationalUnit.getDomains());
    organisationalUnitDto.setAgencyToken(organisationalUnit.getAgencyToken());

    return organisationalUnitDto;
  }

  public OrganisationalUnitDto create(OrganisationalUnit organisationalUnit) {
    return this.create(organisationalUnit, false, true);
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
