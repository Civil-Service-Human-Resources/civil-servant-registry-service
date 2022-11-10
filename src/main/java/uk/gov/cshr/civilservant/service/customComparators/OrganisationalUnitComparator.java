package uk.gov.cshr.civilservant.service.customComparators;

import uk.gov.cshr.civilservant.controller.models.OrganisationalUnitOrderingDirection;
import uk.gov.cshr.civilservant.controller.models.OrganisationalUnitOrderingKey;
import uk.gov.cshr.civilservant.dto.OrganisationalUnitDto;

import java.util.Comparator;

public class OrganisationalUnitComparator implements Comparator<OrganisationalUnitDto> {

    private final OrganisationalUnitOrderingKey orderingKey;
    private final OrganisationalUnitOrderingDirection orderingDirection;

    public OrganisationalUnitComparator(OrganisationalUnitOrderingKey orderingKey,
                                        OrganisationalUnitOrderingDirection orderingDirection) {
        this.orderingKey = orderingKey;
        this.orderingDirection = orderingDirection;
    }

    @Override
    public int compare(OrganisationalUnitDto firstOrg, OrganisationalUnitDto secondOrg) {
        if (this.orderingDirection.equals(OrganisationalUnitOrderingDirection.DESC)) {
            OrganisationalUnitDto temp = firstOrg;
            firstOrg = secondOrg;
            secondOrg = temp;
        }
        if (this.orderingKey == OrganisationalUnitOrderingKey.FORMATTED_NAME) {
            return String.CASE_INSENSITIVE_ORDER.compare(firstOrg.getFormattedName(), secondOrg.getFormattedName());
        }
        return String.CASE_INSENSITIVE_ORDER.compare(firstOrg.getName(), secondOrg.getName());
    }
}
