package uk.gov.cshr.civilservant.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;

@Configuration
public class ResourceServerConfiguration {

  public void configure(HttpSecurity http) throws Exception {
    http
      .sessionManagement(session -> session
              .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
      );
  }
}