package uk.gov.cshr.civilservant.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.cshr.civilservant.domain.AgencyToken;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.dto.*;
import uk.gov.cshr.civilservant.dto.factory.AgencyTokenFactory;
import uk.gov.cshr.civilservant.exception.CSRSApplicationException;
import uk.gov.cshr.civilservant.exception.TokenDoesNotExistException;
import uk.gov.cshr.civilservant.service.OrganisationalUnitService;

import javax.validation.Valid;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

@Slf4j
@RestController
@RequestMapping("/organisationalUnits")
public class OrganisationalUnitController {

    private OrganisationalUnitService organisationalUnitService;

    private AgencyTokenFactory agencyTokenFactory;

    public OrganisationalUnitController(OrganisationalUnitService organisationalUnitService,
                                        AgencyTokenFactory agencyTokenFactory) {
        this.organisationalUnitService = organisationalUnitService;
        this.agencyTokenFactory = agencyTokenFactory;
    }

    @PostMapping("/{organisationalUnitId}/domains")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    @ResponseStatus(HttpStatus.CREATED)
    public AddDomainToOrgResponse addNewDomain(@PathVariable Long organisationalUnitId,
                                               @Valid @RequestBody DomainDto domainDto) {
        return organisationalUnitService.addDomainToOrganisation(organisationalUnitId, domainDto.getDomain());
    }

    @DeleteMapping("/{organisationalUnitId}/domains/{domainId}")
    @PreAuthorize("isAuthenticated()")
    @ResponseBody
    @ResponseStatus(HttpStatus.OK)
    public RemoveDomainFromOrgResponse removeDomainFromOrganisation(@RequestParam(defaultValue = "false") boolean includeSubOrgs,
                                                                    @PathVariable Long organisationalUnitId, @PathVariable Long domainId) {
        return organisationalUnitService.removeDomainFromOrganisation(organisationalUnitId, domainId, includeSubOrgs);
    }

    @PostMapping("/{organisationalUnitId}/agencyToken")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AgencyToken> saveAgencyToken(@PathVariable Long organisationalUnitId, @Valid @RequestBody AgencyTokenDTO agencyTokenDto, UriComponentsBuilder builder) {
        AgencyToken agencyToken = agencyTokenFactory.buildAgencyTokenFromAgencyTokenDto(agencyTokenDto);
        return organisationalUnitService.getOrganisationalUnit(organisationalUnitId).map(organisationalUnit -> {
            organisationalUnitService.setAgencyToken(organisationalUnit, agencyToken);
            return ResponseEntity.status(201).body(agencyToken);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.BAD_REQUEST));
    }

    @GetMapping("/{organisationalUnitId}/agencyToken")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AgencyTokenResponseDto> getAgencyToken(@PathVariable Long organisationalUnitId) {
        try {
            return ResponseEntity.ok(organisationalUnitService.getAgencyToken(organisationalUnitId));
        } catch (TokenDoesNotExistException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (CSRSApplicationException e) {
            log.error("Unexpected error calling getToken: ", e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PatchMapping("/{organisationalUnitId}/agencyToken")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<AgencyToken> updateAgencyToken(@PathVariable Long organisationalUnitId, @Valid @RequestBody AgencyTokenDTO agencyTokenDto) {
        AgencyToken agencyToken = agencyTokenFactory.buildAgencyTokenFromAgencyTokenDto(agencyTokenDto);
        return organisationalUnitService.getOrganisationalUnit(organisationalUnitId).map(organisationalUnit -> {
            organisationalUnitService.updateAgencyToken(organisationalUnit, agencyToken);
            return ResponseEntity.ok(agencyToken);
        }).orElseGet(() -> new ResponseEntity<>(HttpStatus.CONFLICT));
    }

    @DeleteMapping("/{organisationalUnitId}/agencyToken")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity deleteAgencyToken(@PathVariable Long organisationalUnitId) {

        Optional<OrganisationalUnit> organisationalUnit = organisationalUnitService.getOrganisationalUnit(organisationalUnitId);

        if (organisationalUnit.isPresent()) {
           OrganisationalUnit updatedOrgUnit = organisationalUnitService.deleteAgencyToken(organisationalUnit.get());
           if (updatedOrgUnit != null) {
               return new ResponseEntity(HttpStatus.OK);
           } else {
               return new ResponseEntity(HttpStatus.INTERNAL_SERVER_ERROR);
           }
        } else {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @ResponseStatus(HttpStatus.BAD_REQUEST)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Map<String, String> handleValidationExceptions(
            MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        return errors;
    }

}
