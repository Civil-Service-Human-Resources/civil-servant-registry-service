package uk.gov.cshr.civilservant.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import uk.gov.cshr.civilservant.domain.AgencyToken;
import uk.gov.cshr.civilservant.domain.OrganisationalUnit;
import uk.gov.cshr.civilservant.dto.AgencyTokenResponseDto;
import uk.gov.cshr.civilservant.dto.factory.AgencyTokenResponseDtoFactory;
import uk.gov.cshr.civilservant.exception.CSRSApplicationException;
import uk.gov.cshr.civilservant.repository.AgencyTokenRepository;
import uk.gov.cshr.civilservant.repository.OrganisationalUnitRepository;
import uk.gov.cshr.civilservant.service.identity.IdentityService;

import java.util.Optional;

@Service
public class AgencyTokenService {

    private AgencyTokenRepository agencyTokenRepository;
    private OrganisationalUnitRepository organisationalUnitRepository;
    private AgencyTokenResponseDtoFactory agencyTokenResponseDtoFactory;
    private IdentityService identityService;

    public AgencyTokenService(AgencyTokenRepository agencyTokenRepository,
                              OrganisationalUnitRepository organisationalUnitRepository,
                              AgencyTokenResponseDtoFactory agencyTokenResponseDtoFactory,
                              IdentityService identityService) {
        this.agencyTokenRepository = agencyTokenRepository;
        this.organisationalUnitRepository = organisationalUnitRepository;
        this.agencyTokenResponseDtoFactory = agencyTokenResponseDtoFactory;
        this.identityService = identityService;
    }

    public Iterable<AgencyToken> getAllAgencyTokensByDomain(String domain) {
        return agencyTokenRepository.findAllByDomain(domain);
    }

    @Transactional
    public Optional<AgencyToken> getAgencyTokenByDomainTokenCodeAndOrg(String domain, String token, String code) {

        Optional<AgencyToken> agencyToken = agencyTokenRepository.findByDomainAndToken(domain, token);

        if (agencyToken.isPresent()) {
            Optional<OrganisationalUnit> tokenOwner = organisationalUnitRepository.findOrganisationByAgencyToken(agencyToken.get());

            if (tokenOwner.isPresent()) {
                if (organisationContainsCode(tokenOwner.get(), code)) {
                    return agencyToken;
                }
            }
        }

        return Optional.empty();
    }

    private boolean organisationContainsCode(OrganisationalUnit organisationalUnit, String code) {
        if (organisationalUnit.getCode().equals(code)) {
            return true;
        } else if (organisationalUnit.hasChildren()) {
            for (OrganisationalUnit childUnit : organisationalUnit.getChildren()) {
                if  (organisationContainsCode(childUnit, code)) {
                    return true;
                }
            }
        } else {
            return false;
        }
        return false;
    }

    public Optional<AgencyToken> getAgencyTokenByDomainAndToken(String domain, String token) {
        return agencyTokenRepository.findByDomainAndToken(domain, token);
    }

    public void deleteAgencyToken(AgencyToken agencyToken) {
        agencyTokenRepository.delete(agencyToken);
    }

    public boolean isDomainInAgency(String domain) {
        return agencyTokenRepository.existsByDomain(domain);
    }

    public Optional<AgencyToken> getAgencyTokenByUid(String uid) {
        return agencyTokenRepository.findByUid(uid);
    }

    public AgencyTokenResponseDto getAgencyTokenResponseDto(AgencyToken agencyToken) throws CSRSApplicationException {
        int capacityUsed = identityService.getSpacesUsedForAgencyToken(agencyToken.getUid());
        return agencyTokenResponseDtoFactory.buildDto(agencyToken, capacityUsed);
    }

}
