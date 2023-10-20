package uk.gov.cshr.civilservant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.util.Collections;
import java.util.List;

@Data
@AllArgsConstructor
public class RemoveDomainFromOrgResponse {
    Long primaryOrganisationId;
    DomainDto domain;
    List<Long> updatedChildOrganisationIds = Collections.emptyList();

    public RemoveDomainFromOrgResponse(Long primaryOrganisationId, DomainDto domain) {
        this.primaryOrganisationId = primaryOrganisationId;
        this.domain = domain;
    }
}
