package uk.gov.cshr.civilservant.security;

import java.util.Collection;
import java.util.List;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.convert.converter.Converter;
import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class CsrsJwtAuthenticationConverter
        implements Converter<Jwt, AbstractAuthenticationToken> {

    private static final String EMAIL_KEY = "email";
    private static final String IDENTITY_ID_CLAIM = "user_name";

    private final JwtGrantedAuthoritiesConverter authoritiesConverter =
            new JwtGrantedAuthoritiesConverter();

    @Override
    public AbstractAuthenticationToken convert(Jwt jwt) {
        log.debug("Extracting identity details from access token");

        String identityId = jwt.getClaimAsString(IDENTITY_ID_CLAIM);

        if (identityId == null || identityId.isBlank()) {
            throw new IllegalArgumentException(
                    "JWT is missing the identity claim: " + IDENTITY_ID_CLAIM);
        }

        Collection<GrantedAuthority> authorities =
                authoritiesConverter.convert(jwt);

        if (authorities == null) {
            authorities = List.of();
        }

        String email = jwt.getClaimAsString(EMAIL_KEY);

        return new CustomOAuth2Authentication(jwt, authorities, email);
    }
}