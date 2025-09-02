package uk.gov.cshr.civilservant.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.hateoas.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.civilservant.domain.CivilServant;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.dto.CivilServantProfileDto;
import uk.gov.cshr.civilservant.dto.factory.CivilServantProfileDtoFactory;
import uk.gov.cshr.civilservant.exception.CivilServantNotFoundException;
import uk.gov.cshr.civilservant.exception.UserNotFoundException;
import uk.gov.cshr.civilservant.exception.civilServant.InvalidUserOrganisationException;
import uk.gov.cshr.civilservant.exception.organisationalUnit.OrganisationalUnitNotFoundException;
import uk.gov.cshr.civilservant.repository.AgencyTokenRepository;
import uk.gov.cshr.civilservant.repository.CivilServantRepository;
import uk.gov.cshr.civilservant.resource.CivilServantResource;
import uk.gov.cshr.civilservant.resource.factory.CivilServantResourceFactory;
import uk.gov.cshr.civilservant.service.identity.IdentityDTO;
import uk.gov.cshr.civilservant.service.identity.IdentityService;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CivilServantService {

    private final CivilServantRepository civilServantRepository;
    private final IdentityService identityService;
    private final OrganisationalUnitService organisationalUnitService;
    private final AgencyTokenRepository agencyTokenRepository;
    private final CivilServantProfileDtoFactory civilServantProfileDtoFactory;
    private final CivilServantResourceFactory resourceFactory;

    public String getCivilServantUid() {
        CivilServant cs = civilServantRepository.findByPrincipal()
                .orElseThrow(CivilServantNotFoundException::new);
        if (cs.getIdentity() != null && cs.getIdentity().getUid() != null) {
            return cs.getIdentity().getUid();
        }
        throw new CivilServantNotFoundException();
    }

    public Optional<CivilServant> performLogin(IdentityDTO identity) {
        return civilServantRepository.findByPrincipal()
            .map(cs -> {
                String csDomain = identity.getEmailDomain();
                if (!agencyTokenRepository.existsByDomain(csDomain)) {
                    cs.getOrganisationalUnit().ifPresent(o -> {
                        if(!o.doesDomainExist(csDomain) && !identity.isUnrestrictedOrganisation() && !cs.hasOtherOrganisation(o.getId())) {
                            cs.setOrganisationalUnit(null);
                            civilServantRepository.saveAndFlush(cs);
                        }
                    });
                }
                return cs;
            });
    }

    @Transactional
    public CivilServantProfileDto getFullProfile(String uid) {
        return civilServantProfileDtoFactory.create(uid);
    }

    @Transactional
    public Resource<CivilServantResource> getCivilServantResourceWithUid(String uid) {
        return civilServantRepository.findByIdentity(uid).map(
                        resourceFactory::create)
                .orElse(null);
    }

    public void update(CivilServant cs) {
        CivilServant civilServant = civilServantRepository.findByPrincipal()
                .orElseThrow(CivilServantNotFoundException::new);
        if (cs.getFullName() != null) {
            civilServant.setFullName(cs.getFullName());
        }
        if (cs.getGrade() != null) {
            civilServant.setGrade(cs.getGrade());
        }
        if (cs.getProfession().isPresent()) {
            civilServant.setProfession(cs.getProfession().get());
        }
        if (cs.getInterests() != null) {
            civilServant.setInterests(cs.getInterests());
        }
        if (cs.getOtherAreasOfWork() != null) {
            civilServant.setOtherAreasOfWork(cs.getOtherAreasOfWork());
        }
        civilServantRepository.save(civilServant);
    }

    public CivilServant updateMyOrganisationalUnit(Long organisationalUnitId) {
        CivilServant cs = civilServantRepository.findByPrincipal()
                .orElseThrow(CivilServantNotFoundException::new);
        if (cs.getOrganisationalUnit().isPresent() &&
                Objects.equals(cs.getOrganisationalUnit().get().getId(), organisationalUnitId)) {
            log.warn(String.format("Civil servant '%s' tried to update to their current organisational unit (%s). Aborting.", cs.getId(), organisationalUnitId));
            return cs;
        }
        String uid = cs.getIdentity().getUid();
        IdentityDTO identity = identityService.getidentity(uid);
        if (identity != null) {
            OrganisationalUnit organisationalUnit = organisationalUnitService.getOrganisationalUnit(organisationalUnitId)
                    .orElseThrow(() -> new OrganisationalUnitNotFoundException(organisationalUnitId));
            if (identity.getRoles().contains("UNRESTRICTED_ORGANISATION")) {
                log.info("User is an unrestricted organisaton user");
                cs.setOrganisationalUnit(organisationalUnit);
            } else if (cs.hasOtherOrganisation(organisationalUnitId)) {
                log.info("Organisation {} is an other organisational unit for user", organisationalUnitId);
                cs.setOrganisationalUnit(organisationalUnit);
            } else {
                String userDomain = identity.getEmailDomain();
                log.info("Checking domain against organisations");
                boolean valid = organisationalUnitService.isDomainValidForOrganisation(organisationalUnitId, userDomain);
                if (valid) {
                    cs.setOrganisationalUnit(organisationalUnit);
                } else {
                    throw new InvalidUserOrganisationException(String.format("User domain '%s' does not exist on organisation '%s' or any associated agency tokens",
                            userDomain, organisationalUnitId));
                }
            }
            civilServantRepository.saveAndFlush(cs);
        } else {
            log.error(String.format("User '%s' was not found in identity service", uid));
            throw new UserNotFoundException(uid);
        }
        return cs;
    }
}
