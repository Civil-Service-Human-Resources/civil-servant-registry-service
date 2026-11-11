package uk.gov.cshr.civilservant.mapping;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import jakarta.servlet.http.HttpServletRequest;

import lombok.EqualsAndHashCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.servlet.mvc.condition.RequestCondition;

@EqualsAndHashCode
public class RoleRequestCondition implements RequestCondition<RoleRequestCondition> {
  private static final Logger LOG = LoggerFactory.getLogger(RoleRequestCondition.class);

  private Set<String> roles;

  public RoleRequestCondition(Set<String> roles) {
    this.roles = roles;
  }

  public RoleRequestCondition(String... roles) {
    this(new HashSet<>(Arrays.asList(roles)));
  }

  public Set<String> getRoles() {
    return roles;
  }

  @Override
  public RoleRequestCondition combine(RoleRequestCondition other) {
    this.roles.addAll(other.getRoles());
    return this;
  }

  @Override
  public RoleRequestCondition getMatchingCondition(HttpServletRequest request) {
    Set<String> userRoles =
        SecurityContextHolder.getContext()
            .getAuthentication()
            .getAuthorities()
            .stream()
            .map(GrantedAuthority::getAuthority)
            .collect(Collectors.toSet());

    for (String role : userRoles) {
      if (this.roles.contains(role)) {
        return this;
      }
    }

    LOG.debug(
        String.format(
            "Required role not found. Required roles: %s. User roles %s",
            String.join(", ", roles), String.join(", ", userRoles)));
    return null;
  }

  @Override
  public int compareTo(RoleRequestCondition other, HttpServletRequest request) {
    return this.roles.size() - other.getRoles().size();
  }
}
