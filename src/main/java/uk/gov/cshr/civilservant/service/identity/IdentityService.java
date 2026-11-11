package uk.gov.cshr.civilservant.service.identity;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.util.UriComponents;
import org.springframework.web.util.UriComponentsBuilder;
import uk.gov.cshr.civilservant.domain.CivilServant;
import uk.gov.cshr.civilservant.domain.Identity;
import uk.gov.cshr.civilservant.dto.IdentityAgencyResponseDTO;
import uk.gov.cshr.civilservant.exception.CSRSApplicationException;
import uk.gov.cshr.civilservant.exception.TokenDoesNotExistException;
import uk.gov.cshr.civilservant.repository.IdentityRepository;
import uk.gov.cshr.civilservant.service.exception.UserNotFoundException;
import uk.gov.cshr.civilservant.service.identity.model.AgencyTokenCapacityUsed;

import java.util.Objects;
import java.util.Optional;

@Slf4j
@Service
public class IdentityService {

    private RestClient restClient;
    private String identityAPIUrl;
    private String identityAgencyTokenUrl;
    private final UriComponentsBuilder agencyTokenUrlBuilder;
    private final IdentityRepository identityRepository;

    @Autowired
    public IdentityService(RestClient restClient, @Value("${identity.identityAPIUrl}") String identityAPIUrl,
                           @Value("${identity.agencyTokenUrl}") String agencyTokenUrl,
                           @Value("${identity.identityAgencyTokenUrl}") String identityAgencyTokenUrl,
                           IdentityRepository identityRepository) {
        this.restClient = restClient;
        this.identityAPIUrl = identityAPIUrl;
        this.identityAgencyTokenUrl = identityAgencyTokenUrl;
        this.agencyTokenUrlBuilder = UriComponentsBuilder.fromHttpUrl(agencyTokenUrl);
        this.identityRepository = identityRepository;
    }

    public IdentityDTO findByEmail(String email) {

        UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(identityAPIUrl)
                .queryParam("emailAddress", email);

        log.debug(" Checking email {}", email);
        IdentityDTO identity;

        try {
            identity = restClient.get().uri(builder.toUriString()).retrieve().body(IdentityDTO.class);
        } catch (HttpClientErrorException http) {
            if (http.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null; // we kind of have to assume 403 is email not found rather than service not there ...
            }
            log.error(" Error with findByEmail when contacting identity service {}", builder.toUriString());
            return null;
        }

        return identity;
    }

    public IdentityDTO getidentity(String uid) {
        try {
            UriComponentsBuilder builder = UriComponentsBuilder.fromHttpUrl(identityAPIUrl)
                .queryParam("uid", uid);
            return restClient.get().uri(builder.toUriString()).retrieve().body(IdentityDTO.class);
        } catch (HttpClientErrorException e) {
            if (e.getStatusCode() == HttpStatus.NOT_FOUND) {
                return null;
            }
            throw new UserNotFoundException(e);
        }

    }

    public Identity getOrCreateIdentity(String uid) {
                Identity storedIdentity = identityRepository.findByUid(uid)
                .orElseGet(() -> {
                    log.debug("No identity exists for id {}, creating.", uid);
                    return identityRepository.save(new Identity(uid));
                });
                return storedIdentity;
    }

    public String getEmailAddress(CivilServant civilServant) {

        log.debug("Getting email address for civil servant {}", civilServant);
        IdentityDTO identity = getidentity(civilServant.getIdentity().getUid());
        if (identity != null) {
            return identity.getUsername();
        }
        return null;
    }

    public Optional<String> getAgencyTokenUid(String userUid) throws CSRSApplicationException {
        log.debug("Getting the agency token uid from identity service");
        StringBuilder sb = new StringBuilder(identityAgencyTokenUrl);
        sb.append(userUid);

        try {
            ResponseEntity<IdentityAgencyResponseDTO> response = restClient.get().uri(sb.toString()).retrieve().toEntity(IdentityAgencyResponseDTO.class);

            if(response.getStatusCode().is2xxSuccessful() && response.getBody() != null && response.getBody().getAgencyTokenUid() != null) {
                return Optional.of(response.getBody().getAgencyTokenUid());
            } else {
                return Optional.empty();
            }
        } catch (Exception e) {
            log.error("Unexpected error calling identity service: get agency token uid", e);
            throw new CSRSApplicationException("Unexpected error calling identity service: get agency token uid", e);
        }

    }

    public int getSpacesUsedForAgencyToken(String uid) throws CSRSApplicationException {
        UriComponents url = agencyTokenUrlBuilder.buildAndExpand(uid);

        try {
            AgencyTokenCapacityUsed capacityUsed = restClient.get().uri(url.toUriString()).retrieve().body(AgencyTokenCapacityUsed.class);
            return Objects.requireNonNull(capacityUsed).capacityUsed;
        } catch (HttpClientErrorException clientError) {
            if(clientError.getStatusCode() == HttpStatus.NOT_FOUND) {
                log.warn("Token for uid " + uid + " does not exist");
                throw new TokenDoesNotExistException(uid);
            } else {
                throw new CSRSApplicationException("Error calling identity service: get Agency Tokens Spaces Used", clientError);
            }
        } catch (HttpServerErrorException serverError) {
            throw new CSRSApplicationException("Server error calling identity service: get Agency Tokens Spaces Used", serverError);

        } catch (Exception e) {
            throw new CSRSApplicationException("Unexpected error calling identity service: get Agency Tokens Spaces Used", e);
        }

    }

    public void removeAgencyTokenFromUsers(String agencyTokenUid) throws CSRSApplicationException {
        log.debug("Removing agency token");
        UriComponents url = agencyTokenUrlBuilder.buildAndExpand(agencyTokenUid);

        try {
            restClient.delete().uri(url.toUriString());
        } catch (HttpClientErrorException clientError) {
            throw new CSRSApplicationException("Error calling identity service: delete agency token", clientError);
        } catch (HttpServerErrorException serverError) {
            throw new CSRSApplicationException("Server error calling identity service: delete agency token", serverError);
        } catch (Exception e) {
            throw new CSRSApplicationException("Unexpected error calling identity service: delete agency token", e);
        }
    }

}
