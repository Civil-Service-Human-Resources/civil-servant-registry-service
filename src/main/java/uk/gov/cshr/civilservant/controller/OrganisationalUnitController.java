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
import uk.gov.cshr.civilservant.dto.factory.OrganisationalUnitDtoFactory;
import uk.gov.cshr.civilservant.exception.CSRSApplicationException;
import uk.gov.cshr.civilservant.exception.CivilServantNotFoundException;
import uk.gov.cshr.civilservant.exception.NoOrganisationsFoundException;
import uk.gov.cshr.civilservant.exception.TokenDoesNotExistException;
import uk.gov.cshr.civilservant.service.CivilServantService;
import uk.gov.cshr.civilservant.service.OrganisationalUnitService;

import javax.validation.Valid;
import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/organisationalUnits")
public class OrganisationalUnitController {

    private OrganisationalUnitService organisationalUnitService;

    private OrganisationalUnitDtoFactory organisationalUnitDtoFactory;

    private AgencyTokenFactory agencyTokenFactory;

    private CivilServantService civilServantService;

    public OrganisationalUnitController(OrganisationalUnitService organisationalUnitService,
                                        OrganisationalUnitDtoFactory organisationalUnitDtoFactory,
                                        AgencyTokenFactory agencyTokenFactory,
                                        CivilServantService civilServantService) {
        this.organisationalUnitService = organisationalUnitService;
        this.organisationalUnitDtoFactory = organisationalUnitDtoFactory;
        this.agencyTokenFactory = agencyTokenFactory;
        this.civilServantService = civilServantService;
    }

    @GetMapping("/tree")
    public ResponseEntity<List<OrganisationalUnit>> listOrganisationalUnitsAsTreeStructure() {
        log.info("Getting org tree");
        return ResponseEntity.ok(organisationalUnitService.getOrgTree());
    }

    @GetMapping("/flat")
    public ResponseEntity<List<OrganisationalUnitDto>> listOrganisationalUnitsAsFlatStructure() {
        log.info("Getting org flat");
        return ResponseEntity.ok(organisationalUnitService.getFlatOrg());
    }

    @GetMapping("/flat/{domain}/")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<List<OrganisationalUnitDto>> listOrganisationalUnitsAsFlatStructureFilteredByDomain(@PathVariable String domain) {
        log.info("Getting org flat, filtered by domain");
        List<OrganisationalUnit> organisationalUnits;
        try {
            String uid = civilServantService.getCivilServantUid();
            try {
                organisationalUnits = organisationalUnitService.getOrganisationsForDomain(domain, uid);
            } catch (NoOrganisationsFoundException e) {
                return ResponseEntity.ok(Collections.EMPTY_LIST);
            }
            if(organisationalUnits.isEmpty()) {
                return ResponseEntity.ok(Collections.EMPTY_LIST);
            }
            List<OrganisationalUnitDto> dtos = organisationalUnits.stream()
                    .map(ou -> organisationalUnitDtoFactory.create(ou))
                    .sorted(Comparator.comparing(OrganisationalUnitDto::getFormattedName, String.CASE_INSENSITIVE_ORDER))
                    .collect(Collectors.toList());
            return ResponseEntity.ok(dtos);
        } catch(CivilServantNotFoundException | TokenDoesNotExistException e) {
            return ResponseEntity.ok(Collections.EMPTY_LIST);
        } catch(Exception e) {
            log.error("Unexpected error occurred getting orgs filtered by domain", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/children/{code}")
    public ResponseEntity<List<OrganisationalUnit>> getOrganisationWithChildren(@PathVariable String code) {
        log.info("Getting org for current family only, current and any children");
        return ResponseEntity.ok(organisationalUnitService.getOrganisationWithChildren(code));
    }

    @GetMapping("/parent/{code}")
    public ResponseEntity<List<OrganisationalUnit>> getOrganisationWithParents(@PathVariable String code) {
        return ResponseEntity.ok(organisationalUnitService.getOrganisationWithParents(code));
    }

    @GetMapping("/normalised")
    public ResponseEntity<List<OrganisationalUnit>> getOrganisationNormalised() {
        return ResponseEntity.ok(organisationalUnitService.getOrganisationsNormalised());
    }

    @GetMapping("/allCodesMap")
    public ResponseEntity<Map<String, List<String>>> getAllCodes() {
        Map<String, List<String>> codeParentCodesMap = new HashMap<>();

        List<String> organisationalUnitCodes = organisationalUnitService.getOrganisationalUnitCodes();

        organisationalUnitCodes.forEach(s -> {
            List<String> parentCodes = organisationalUnitService.getOrganisationWithParents(s)
                    .stream()
                    .map(OrganisationalUnit::getCode)
                    .collect(Collectors.toList());
            codeParentCodesMap.put(s, parentCodes);
        });

        return ResponseEntity.ok(codeParentCodesMap);
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
    public ResponseEntity saveAgencyToken(@PathVariable Long organisationalUnitId, @Valid @RequestBody AgencyTokenDTO agencyTokenDto, UriComponentsBuilder builder) {
        AgencyToken agencyToken = agencyTokenFactory.buildAgencyTokenFromAgencyTokenDto(agencyTokenDto);
        return organisationalUnitService.getOrganisationalUnit(organisationalUnitId).map(organisationalUnit -> {
            organisationalUnitService.setAgencyToken(organisationalUnit, agencyToken);
            return ResponseEntity.created(builder.path("/organisationalUnits/{organisationalUnitId}/agencyToken").build(organisationalUnit.getId())).build();
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
    public ResponseEntity updateAgencyToken(@PathVariable Long organisationalUnitId, @Valid @RequestBody AgencyTokenDTO agencyTokenDto) {
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
