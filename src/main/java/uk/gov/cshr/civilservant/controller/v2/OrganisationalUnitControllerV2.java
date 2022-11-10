package uk.gov.cshr.civilservant.controller.v2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.cshr.civilservant.controller.models.OrganisationalUnitOrderingDirection;
import uk.gov.cshr.civilservant.controller.models.OrganisationalUnitOrderingKey;
import uk.gov.cshr.civilservant.controller.models.OrganisationalUnitResponse;
import uk.gov.cshr.civilservant.dto.OrganisationalUnitDto;
import uk.gov.cshr.civilservant.service.OrganisationalUnitService;

import java.util.List;
import java.util.Locale;

@Slf4j
@RestController
@RequestMapping("/v2/organisationalUnits")
public class OrganisationalUnitControllerV2 {

    private final OrganisationalUnitService organisationalUnitService;

    public OrganisationalUnitControllerV2(OrganisationalUnitService organisationalUnitService) {
        this.organisationalUnitService = organisationalUnitService;
    }

    @GetMapping("/{organisationalUnitId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrganisationalUnitDto> getOrganisation(
            @PathVariable(value = "organisationalUnitId") Long id,
            @RequestParam(value = "includeFormattedName", required = false, defaultValue = "false") boolean includeFormattedName,
            @RequestParam(value = "includeParents", required = false, defaultValue = "false") boolean includeParents
    ) {
        OrganisationalUnitDto organisationalUnitDto = organisationalUnitService.getOrganisationalUnit(id, includeFormattedName, includeParents);
        return ResponseEntity.ok(organisationalUnitDto);
    }

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrganisationalUnitResponse> listOrganisations(
            @RequestParam(value = "ids", required = false, defaultValue = "") List<Long> ids,
            @RequestParam(value = "includeFormattedName", required = false, defaultValue = "false") boolean includeFormattedName,
            @RequestParam(value = "orderBy", required = false, defaultValue = "NAME") OrganisationalUnitOrderingKey orderBy,
            @RequestParam(value = "orderDirection", required = false, defaultValue = "ASC") OrganisationalUnitOrderingDirection orderDirection
    ) {
        if (orderBy.equals(OrganisationalUnitOrderingKey.FORMATTED_NAME) && !includeFormattedName) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Cannot order by formattedName when includeFormattedName is false");
        }
        List<OrganisationalUnitDto> organisationalUnitDtos = organisationalUnitService.getOrganisationalUnits(
                ids, includeFormattedName, orderBy, orderDirection);
        return ResponseEntity.ok(new OrganisationalUnitResponse(organisationalUnitDtos));
    }

}
