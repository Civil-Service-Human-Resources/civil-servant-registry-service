package uk.gov.cshr.civilservant.service;

import com.google.common.collect.Lists;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import uk.gov.cshr.civilservant.controller.v2.models.GetOrganisationalUnitsParams;
import uk.gov.cshr.civilservant.controller.v2.models.SimplePage;
import uk.gov.cshr.civilservant.domain.AgencyToken;
import uk.gov.cshr.civilservant.domain.Domain;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.dto.*;
import uk.gov.cshr.civilservant.dto.factory.OrganisationalUnitDtoFactory;
import uk.gov.cshr.civilservant.exception.*;
import uk.gov.cshr.civilservant.exception.organisationalUnit.DomainAlreadyExistsException;
import uk.gov.cshr.civilservant.exception.organisationalUnit.OrganisationalUnitNotFoundException;
import uk.gov.cshr.civilservant.repository.CivilServantRepository;
import uk.gov.cshr.civilservant.repository.DomainRepository;
import uk.gov.cshr.civilservant.repository.OrganisationalUnitRepository;
import uk.gov.cshr.civilservant.service.identity.IdentityService;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
@Service
@Transactional
public class OrganisationalUnitService extends SelfReferencingEntityService<OrganisationalUnit, OrganisationalUnitDto> {

    private final OrganisationalUnitRepository repository;
    private final OrganisationalUnitDtoFactory dtoFactory;
    private final DomainRepository domainRepository;
    private final CivilServantRepository civilServantRepository;
    private final AgencyTokenService agencyTokenService;
    private final IdentityService identityService;

    public OrganisationalUnitService(OrganisationalUnitRepository organisationalUnitRepository,
                                     OrganisationalUnitDtoFactory organisationalUnitDtoFactory,
                                     DomainRepository domainRepository,
                                     CivilServantRepository civilServantRepository, AgencyTokenService agencyTokenService,
                                     IdentityService identityService) {
        super(organisationalUnitRepository, organisationalUnitDtoFactory);
        this.repository = organisationalUnitRepository;
        this.dtoFactory = organisationalUnitDtoFactory;
        this.domainRepository = domainRepository;
        this.civilServantRepository = civilServantRepository;
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
            Set<OrganisationalUnit> listOfChildren = organisationalUnit.getChildren();
            listOfChildren.stream().forEach(childOrganisationalUnit -> getOrganisationalUnitAndChildren(childOrganisationalUnit.getCode(), organisationalUnits));
        }
    }

    public Optional<OrganisationalUnit> getOrganisationalUnit(Long id) {
        return repository.findById(id);
    }

    public OrganisationalUnitDto getOrganisationalUnit(Long id, boolean includeParents) {
        OrganisationalUnit organisationalUnit = getOrganisationalUnit(id).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        return dtoFactory.create(organisationalUnit, includeParents, false, false);
    }

    public SimplePage<OrganisationalUnitDto> getOrganisationalUnits(Pageable pageable, GetOrganisationalUnitsParams params) {
        Page<OrganisationalUnit> organisationalUnitPage;
        if (params.getIds().isEmpty()) {
            organisationalUnitPage = repository.findAll(pageable);
        } else {
            organisationalUnitPage = repository.findAllByIdIn(params.getIds(), pageable);
        }
        return new SimplePage<>(organisationalUnitPage.getContent()
                .stream()
                .map(o -> dtoFactory.create(o, false, false, params.isFetchChildren()))
                .collect(Collectors.toList()), organisationalUnitPage.getTotalElements(), pageable);
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

    public OrganisationalUnit getOrThrowIfNotFound(Long id) {
        return repository.findById(id).orElseThrow(
                () -> new OrganisationalUnitNotFoundException(id));
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
                    List<OrganisationalUnit> children = org.getChildrenAsList();
                    children.sort(Comparator.comparing(OrganisationalUnit::getName, String.CASE_INSENSITIVE_ORDER));
                    //Below line is a recursive call which will be called recursively
                    //until there are children as per above if condition.
                    sortOrganisationList(children);
                }
            }
        );
    }

    public AddDomainToOrgResponse addDomainToOrganisation(Long organisationalUnitId, String domainString) {
        OrganisationalUnit organisationalUnit = repository.findById(organisationalUnitId).orElseThrow(
                () -> new OrganisationalUnitNotFoundException(organisationalUnitId));
        Domain domain = domainRepository.findDomainByDomain(domainString)
                .orElseGet(() -> domainRepository.save(new Domain(domainString)));
        if (organisationalUnit.doesDomainExist(domainString)) {
            throw new DomainAlreadyExistsException(String.format("Domain '%s' already exists on organisation '%s'",
                    domain.getDomain(), organisationalUnit.getName()));
        }
        organisationalUnit.addDomain(domain);
        repository.saveAndFlush(organisationalUnit);
        AddDomainToOrgResponse response = new AddDomainToOrgResponse(organisationalUnitId, new DomainDto(domain));
        if (organisationalUnit.hasChildren()) {
            List<OrganisationalUnit> flatList = organisationalUnit.getDescendantsAsFlatList();
            BulkUpdate<OrganisationalUnit> bulkResponse = bulkAddDomainToOrganisations(flatList, domain);
            response.setUpdatedChildOrganisationIds(bulkResponse.getUpdatedIds());
            response.setSkippedChildOrganisationIds(bulkResponse.getSkippedIds());
        }
        return response;
    }

    public BulkUpdate<OrganisationalUnit> bulkAddDomainToOrganisations(List<OrganisationalUnit> organisationalUnits, Domain domain) {
        BulkUpdate<OrganisationalUnit> res = new BulkUpdate<>();
        organisationalUnits.forEach(o -> {
            if (o.doesDomainExist(domain.getDomain())) {
                log.info(String.format("Domain '%s' already exists on organisation '%s', skipping.", domain.getDomain(), o.getName()));
                res.getSkipped().add(o);
            } else {
                log.info(String.format("Domain '%s' does not exist on organisation '%s', adding.", domain.getDomain(), o.getName()));
                o.addDomain(domain);
                res.getUpdated().add(o);
            }
        });
        repository.saveAll(res.getUpdated());
        return res;
    }

    public BulkUpdate<OrganisationalUnit> bulkRemoveDomainFromOrganisations(List<OrganisationalUnit> organisationalUnits, Domain domain) {
        BulkUpdate<OrganisationalUnit> res = new BulkUpdate<>();
        log.info(String.format("Attempting to remove domain '%s' from %s organisations", domain.getDomain(), organisationalUnits.size()));
        organisationalUnits.forEach(o -> {
            if (o.doesDomainExist(domain.getDomain())) {
                log.info(String.format("Domain '%s' exists on organisation '%s', removing.", domain.getDomain(), o.getName()));
                o.removeDomain(domain);
                res.getUpdated().add(o);
            }
        });
        repository.saveAll(res.getUpdated());
        repository.flush();
        return res;
    }

    public RemoveDomainFromOrgResponse removeDomainFromOrganisation(Long organisationalUnitId, Long domainId,
                                                                    boolean includeSubOrgs) {
        OrganisationalUnit organisationalUnit = getOrThrowIfNotFound(organisationalUnitId);
        Domain domain = domainRepository.findById(domainId).orElseThrow(
                () -> new NotFoundException(String.format("Domain with ID '%s' not found", domainId)));
        List<OrganisationalUnit> orgsForUpdate = Lists.newArrayList(organisationalUnit);
        if (includeSubOrgs && organisationalUnit.hasChildren()) {
            orgsForUpdate.addAll(organisationalUnit.getDescendantsAsFlatList());
        }
        BulkUpdate<OrganisationalUnit> bulkResponse = bulkRemoveDomainFromOrganisations(orgsForUpdate, domain);

        if (domain.getOrganisationalUnits().size() == 0) {
            log.info(String.format("Domain '%s' is no longer assigned to any organisations. Deleting.", domain.getDomain()));
            domainRepository.delete(domain);
        } else {
            log.info(String.format("Domain '%s' is still assigned to %s organisations, not deleting", domain.getDomain(), domain.getOrganisationalUnits().size()));
        }
        return RemoveDomainFromOrgResponse.fromBulkUpdate(organisationalUnitId, new DomainDto(domain), bulkResponse);
    }
}

