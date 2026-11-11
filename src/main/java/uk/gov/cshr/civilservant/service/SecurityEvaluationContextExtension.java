package uk.gov.cshr.civilservant.service;

import org.springframework.data.spel.spi.EvaluationContextExtension;
import org.springframework.security.access.expression.SecurityExpressionRoot;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
public class SecurityEvaluationContextExtension
        implements EvaluationContextExtension {

  @Override
  public String getExtensionId() {
    return "security";
  }

  @Override
  public SecurityExpressionRoot getRootObject() {
    Authentication authentication =
            SecurityContextHolder.getContext().getAuthentication();

    return new SecurityExpressionRoot(authentication) {};
  }
}