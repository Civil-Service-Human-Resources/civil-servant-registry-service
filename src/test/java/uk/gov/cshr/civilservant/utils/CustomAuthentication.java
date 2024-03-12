package uk.gov.cshr.civilservant.utils;

import org.springframework.security.authentication.AbstractAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;

import java.util.Collection;

public class CustomAuthentication extends AbstractAuthenticationToken {

    private final String username;

    /**
     * Creates a token with the supplied array of authorities.
     *
     * @param authorities the collection of <tt>GrantedAuthority</tt>s for the principal
     *                    represented by this authentication object.
     */
    public CustomAuthentication(Collection<? extends GrantedAuthority> authorities,
                                String username) {
        super(authorities);
        this.username = username;
    }

    @Override
    public Object getCredentials() {
        return username;
    }

    @Override
    public Object getPrincipal() {
        return username;
    }
}
