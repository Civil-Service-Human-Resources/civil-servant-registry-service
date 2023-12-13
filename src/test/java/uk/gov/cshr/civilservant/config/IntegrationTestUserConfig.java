package uk.gov.cshr.civilservant.config;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.security.authentication.AuthenticationProvider;
import uk.gov.cshr.civilservant.utils.CustomAuthProvider;

@TestConfiguration
public class IntegrationTestUserConfig {
    @Bean
    public AuthenticationProvider authenticationProvider() {
        return new CustomAuthProvider();
    }
}
