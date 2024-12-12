package uk.gov.cshr.civilservant.security;

import com.google.common.collect.ImmutableSet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.access.intercept.RunAsUserToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.store.JwtAccessTokenConverter;
import uk.gov.cshr.civilservant.domain.CivilServant;
import uk.gov.cshr.civilservant.domain.Identity;
import uk.gov.cshr.civilservant.repository.CivilServantRepository;
import uk.gov.cshr.civilservant.repository.IdentityRepository;

import java.util.Map;
import java.util.Optional;

@Slf4j
public class CsrsJwtAccessTokenConverter extends JwtAccessTokenConverter {

  private static final String INTERNAL_ROLE = "INTERNAL";
  private final String EMAIL_KEY = "email";

  private final IdentityRepository identityRepository;

  private final CivilServantRepository civilServantRepository;

  public CsrsJwtAccessTokenConverter(
      IdentityRepository identityRepository, CivilServantRepository civilServantRepository) {
    this.identityRepository = identityRepository;
    this.civilServantRepository = civilServantRepository;
  }

    @Override
    public Map<String, ?> convertAccessToken(OAuth2AccessToken token, OAuth2Authentication authentication) {
        log.debug("Getting user email address from access token");
        Map<String, Object> vals = (Map<String, Object>) super.convertAccessToken(token, authentication);
        vals.put(EMAIL_KEY, token.getAdditionalInformation().get(EMAIL_KEY));
        return vals;
    }

    @Override
  public CustomOAuth2Authentication extractAuthentication(Map<String, ?> map) {
    OAuth2Authentication authentication = super.extractAuthentication(map);

    configureInternalUser();

    String identityId = (String) authentication.getPrincipal();

    Optional<Identity> identity = Optional.empty();

    try {
      identity = identityRepository.findByUid(identityId);
    } catch (Exception e) {
      log.error(e.getMessage());
    }

    Identity storedIdentity =
        identity.orElseGet(
            () -> {
              log.debug("No identity exists for id {}, creating.", identityId);
              Identity newIdentity = new Identity(identityId);
              return identityRepository.save(newIdentity);
            });

    Optional<CivilServant> civilServant = Optional.empty();

    try {
      civilServant = civilServantRepository.findByIdentity(storedIdentity);
    } catch (Exception e) {
      log.error(e.getMessage());
    }

    civilServant.orElseGet(
        () -> {
          log.debug("No civil servant exists for identity {}, creating.", storedIdentity);
          CivilServant newCivilServant = new CivilServant(storedIdentity);
          return civilServantRepository.save(newCivilServant);
        });

    // email will be null here if the token is a client token
    String email = (String) map.get(EMAIL_KEY);
    return new CustomOAuth2Authentication(authentication.getOAuth2Request(), authentication.getUserAuthentication(), email);
  }

  private void configureInternalUser() {
    SecurityContext securityContext = SecurityContextHolder.getContext();

    securityContext.setAuthentication(
        new RunAsUserToken(
            INTERNAL_ROLE,
            null,
            null,
            ImmutableSet.of(new SimpleGrantedAuthority(INTERNAL_ROLE)),
            null));
  }
}
