package uk.gov.cshr.civilservant.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;

public class CustomAuthProvider implements AuthenticationProvider {
    private static final Logger log = LoggerFactory.getLogger(CustomAuthProvider.class);

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        return authentication;
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return true;
    }
}
