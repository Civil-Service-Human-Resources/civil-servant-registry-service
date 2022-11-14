package uk.gov.cshr.civilservant.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.cshr.civilservant.controller.models.OrganisationalUnitOrderingDirection;
import uk.gov.cshr.civilservant.controller.models.OrganisationalUnitOrderingKey;
import uk.gov.cshr.civilservant.domain.AgencyToken;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.dto.AgencyTokenResponseDto;
import uk.gov.cshr.civilservant.dto.OrganisationalUnitDto;
import uk.gov.cshr.civilservant.dto.factory.OrganisationalUnitDtoFactory;
import uk.gov.cshr.civilservant.exception.CSRSApplicationException;
import uk.gov.cshr.civilservant.exception.NoOrganisationsFoundException;
import uk.gov.cshr.civilservant.exception.TokenAlreadyExistsException;
import uk.gov.cshr.civilservant.exception.TokenDoesNotExistException;
import uk.gov.cshr.civilservant.repository.OrganisationalUnitRepository;
import uk.gov.cshr.civilservant.service.comparator.OrganisationalUnitComparator;
import uk.gov.cshr.civilservant.service.identity.IdentityService;

import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
@Service
@Transactional
public class OrganisationalUnitService extends SelfReferencingEntityService<OrganisationalUnit, OrganisationalUnitDto> {

    private OrganisationalUnitRepository repository;
    private OrganisationalUnitDtoFactory dtoFactory;
    private AgencyTokenService agencyTokenService;
    private IdentityService identityService;

    public OrganisationalUnitService(OrganisationalUnitRepository organisationalUnitRepository,
                                     OrganisationalUnitDtoFactory organisationalUnitDtoFactory,
                                     AgencyTokenService agencyTokenService,
                                     IdentityService identityService) {
        super(organisationalUnitRepository, organisationalUnitDtoFactory);
        this.dtoFactory = organisationalUnitDtoFactory;
        this.repository = organisationalUnitRepository;
        this.agencyTokenService = agencyTokenService;
        this.identityService = identityService;
    }

    public List<OrganisationalUnit> getOrganisationWithParents(String code) {
        List<OrganisationalUnit> organisationalUnitList = new ArrayList<>();
        getOrganisationalUnitAndParent(code, organisationalUnitList);
        sortOrganisationList(organisationalUnitList);
        return organisationalUnitList;
    }

    public List<OrganisationalUnit> getOrganisationWithChildren(String code) {
        List<OrganisationalUnit> organisationalUnitList = new ArrayList<>();
        getOrganisationalUnitAndChildren(code, organisationalUnitList);
        sortOrganisationList(organisationalUnitList);
        return organisationalUnitList;
    }

    public List<OrganisationalUnit> getOrganisationsForDomain(String domain, String userUid) throws CSRSApplicationException {
        return identityService.getAgencyTokenUid(userUid)
                .map(s -> {
                    AgencyToken agencyToken = agencyTokenService.getAgencyTokenByUid(s)
                            .orElseThrow(TokenDoesNotExistException::new);

                    OrganisationalUnit organisationalUnit = repository.findOrganisationByAgencyToken(agencyToken)
                            .orElseThrow(() -> new NoOrganisationsFoundException((domain)));

                    return getOrganisationWithChildren(organisationalUnit.getCode());
                })
                .orElseGet(() -> repository.findAll());
    }

    private List<OrganisationalUnit> getOrganisationalUnitAndChildren(String code, List<OrganisationalUnit> organisationalUnits) {
        repository.findByCode(code).ifPresent(organisationalUnit -> {
            organisationalUnits.add(organisationalUnit);
            getChildren(organisationalUnit, organisationalUnits);
        });

        return organisationalUnits;
    }

    private List<OrganisationalUnit> getOrganisationalUnitAndParent(String code, List<OrganisationalUnit> organisationalUnits) {
        repository.findByCode(code).ifPresent(organisationalUnit -> {
            organisationalUnits.add(organisationalUnit);
            getParent(organisationalUnit, organisationalUnits);
        });

        return organisationalUnits;
    }

    private void getParent(OrganisationalUnit organisationalUnit, List<OrganisationalUnit> organisationalUnits) {
        Optional<OrganisationalUnit> parent = Optional.ofNullable(organisationalUnit.getParent());
        parent.ifPresent(parentOrganisationalUnit -> getOrganisationalUnitAndParent(parentOrganisationalUnit.getCode(), organisationalUnits));
    }

    private void getChildren(OrganisationalUnit organisationalUnit, List<OrganisationalUnit> organisationalUnits) {
        if (organisationalUnit.hasChildren()) {
            List<OrganisationalUnit> listOfChildren = organisationalUnit.getChildren();
            listOfChildren.stream().forEach(childOrganisationalUnit -> getOrganisationalUnitAndChildren(childOrganisationalUnit.getCode(), organisationalUnits));
        }
    }

    public Optional<OrganisationalUnit> getOrganisationalUnit(Long id) {
        return repository.findById(id);
    }

    public List<OrganisationalUnit> getOrganisationsNormalised() {
        List<OrganisationalUnit> organisationalUnits = repository.findAllNormalised();
        sortOrganisationList(organisationalUnits);
        return organisationalUnits;
    }

    public OrganisationalUnit setAgencyToken(OrganisationalUnit organisationalUnit, AgencyToken agencyToken) {
        if (organisationalUnit.getAgencyToken() != null) {
            throw new TokenAlreadyExistsException(organisationalUnit.getId().toString());
        }

        organisationalUnit.setAgencyToken(agencyToken);

        return repository.save(organisationalUnit);
    }

    public OrganisationalUnit updateAgencyToken(OrganisationalUnit organisationalUnit, AgencyToken newToken) {
        AgencyToken currentToken = organisationalUnit.getAgencyToken();

        if (currentToken == null) {
             throw new TokenDoesNotExistException(organisationalUnit.getId().toString());
        }

        currentToken.setAgencyDomains(newToken.getAgencyDomains());
        currentToken.setCapacity(newToken.getCapacity());
        currentToken.setToken(newToken.getToken());

        return repository.save(organisationalUnit);
    }

    public OrganisationalUnit deleteAgencyToken(OrganisationalUnit organisationalUnit) {

        AgencyToken agencyToken = organisationalUnit.getAgencyToken();

        try {
            identityService.removeAgencyTokenFromUsers(agencyToken.getUid());
        } catch (CSRSApplicationException e) {
            log.error("Error removing users from agency token (%s) to be deleted, error is: %s", agencyToken.getUid(), e.getMessage());
            return null;
        }

        organisationalUnit.setAgencyToken(null);
        OrganisationalUnit updateOrgUnit = repository.save(organisationalUnit);

        agencyTokenService.deleteAgencyToken(agencyToken);

        return updateOrgUnit;
    }

    public List<String> getOrganisationalUnitCodes() {
        List<String> allCodes = repository.findAllCodes();
        Collections.sort(allCodes, String.CASE_INSENSITIVE_ORDER);
        return allCodes;
    }

    @Transactional
    public Optional<OrganisationalUnit> get(Long id) {
        return repository.findById(id);
    }

    public AgencyTokenResponseDto getAgencyToken(Long organisationalUnitId) throws CSRSApplicationException {
        AgencyToken agencyToken = getOrganisationalUnit(organisationalUnitId)
                .map(OrganisationalUnit::getAgencyToken)
                .orElseThrow(TokenDoesNotExistException::new);

        return agencyTokenService.getAgencyTokenResponseDto(agencyToken);
    }

    public List<OrganisationalUnit> getOrgTree() {
        List<OrganisationalUnit> listOrg = this.getParents();
        sortOrganisationList(listOrg);
        return listOrg;
    }

    public List<OrganisationalUnitDto> getFlatOrg() {
        return this.getListSortedByValue();
    }

    private void sortOrganisationList(List<OrganisationalUnit> list) {
        list.forEach(org ->
            {
                if(org.hasChildren()) {
                    List<OrganisationalUnit> children = org.getChildren();
                    children.sort(Comparator.comparing(OrganisationalUnit::getName, String.CASE_INSENSITIVE_ORDER));
                    //Below line is a recursive call which will be called recursively
                    //until there are children as per above if condition.
                    sortOrganisationList(children);
                }
            }
        );
    }

    public OrganisationalUnitDto getOrganisationalUnit(Long id, boolean includeFormattedName, boolean includeParents) {
        OrganisationalUnit organisationalUnit = repository.findById(id).orElseThrow(
                () -> new ResponseStatusException(HttpStatus.NOT_FOUND, String.format("Organisational Unit with id %s was not found", id))
        );
        return dtoFactory.create(organisationalUnit, includeFormattedName, includeParents);
    }

    public List<OrganisationalUnitDto> getOrganisationalUnits(boolean includeFormattedName,
                                                              OrganisationalUnitOrderingKey orderBy,
                                                              OrganisationalUnitOrderingDirection orderDirection) {
        List<OrganisationalUnit> organisations = repository.findAll();
        Stream<OrganisationalUnitDto> dtos = organisations
                .stream()
                .map(o -> dtoFactory.create(o, includeFormattedName, false));

        if (orderBy != null && orderDirection != null) {
            Comparator<OrganisationalUnitDto> comparator = new OrganisationalUnitComparator(orderBy, orderDirection);
            dtos = dtos.sorted(comparator);
        }

        return dtos.collect(Collectors.toList());
    }

}
