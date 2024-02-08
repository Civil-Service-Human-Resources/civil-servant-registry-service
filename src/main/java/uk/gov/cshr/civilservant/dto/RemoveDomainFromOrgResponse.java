package uk.gov.cshr.civilservant.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Data
@AllArgsConstructor
public class RemoveDomainFromOrgResponse {
    Long primaryOrganisationId;
    DomainDto domain;
    List<Long> updatedChildOrganisationIds;

    public static RemoveDomainFromOrgResponse fromBulkUpdate(Long primaryOrganisationId, DomainDto domain,
                                                             BulkUpdate<OrganisationalUnit> bulkUpdate) {
        return new RemoveDomainFromOrgResponse(primaryOrganisationId, domain, bulkUpdate
                .getUpdatedIds()
                .stream().filter(id -> !Objects.equals(id, primaryOrganisationId))
                .collect(Collectors.toList()));
    }
}
