package uk.gov.cshr.civilservant.config;

import org.springframework.boot.autoconfigure.web.servlet.WebMvcRegistrations;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.web.servlet.mvc.method.annotation.RequestMappingHandlerMapping;
import uk.gov.cshr.civilservant.mapping.RoleMappingHandlerMapping;

@Configuration
public class RequestMappingConfig implements WebMvcRegistrations {

  @Override
  public RequestMappingHandlerMapping getRequestMappingHandlerMapping() {
    RoleMappingHandlerMapping mapping = new RoleMappingHandlerMapping();
    mapping.setOrder(Ordered.HIGHEST_PRECEDENCE);
    return mapping;
  }
}