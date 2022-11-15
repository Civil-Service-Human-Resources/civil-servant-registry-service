package uk.gov.cshr.civilservant.controller.v2;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
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
            @RequestParam(value = "includeParents", required = false, defaultValue = "false") boolean includeParents
    ) {
        OrganisationalUnitDto organisationalUnitDto = organisationalUnitService.getOrganisationalUnit(id, includeParents);
        return ResponseEntity.ok(organisationalUnitDto);
    }

    @GetMapping()
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<OrganisationalUnitResponse> listOrganisations() {
        List<OrganisationalUnitDto> organisationalUnitDtos = organisationalUnitService.getOrganisationalUnits();
        return ResponseEntity.ok(new OrganisationalUnitResponse(organisationalUnitDtos));
    }

}
