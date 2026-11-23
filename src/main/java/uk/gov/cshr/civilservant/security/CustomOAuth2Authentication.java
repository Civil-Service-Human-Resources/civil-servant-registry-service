package uk.gov.cshr.civilservant.security;

import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;

import java.util.Collection;
import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class CustomOAuth2Authentication extends JwtAuthenticationToken {

    private final String userEmail;

    public CustomOAuth2Authentication(
            Jwt jwt,
            Collection<? extends GrantedAuthority> authorities,
            String userEmail) {
        super(jwt, authorities, jwt.getClaimAsString("user_name"));
        this.userEmail = userEmail;
    }

    public Set<String> getRoles() {
        return getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toSet());
    }

    public String getUid() {
        return getName();
    }
}