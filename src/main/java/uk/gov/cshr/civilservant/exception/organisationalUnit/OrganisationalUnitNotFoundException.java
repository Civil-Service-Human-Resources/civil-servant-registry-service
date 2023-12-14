package uk.gov.cshr.civilservant.exception.organisationalUnit;

import uk.gov.cshr.civilservant.exception.NotFoundException;

public class OrganisationalUnitNotFoundException extends NotFoundException {

    public OrganisationalUnitNotFoundException(Long organisationalUnitId) {
        super(String.format("Organisational unit with id '%s' not found", organisationalUnitId));
    }
}
