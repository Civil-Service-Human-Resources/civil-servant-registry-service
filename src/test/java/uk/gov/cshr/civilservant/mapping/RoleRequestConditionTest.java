package uk.gov.cshr.civilservant.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.mockito.Mockito.*;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import jakarta.servlet.http.HttpServletRequest;

import org.junit.After;
import org.junit.Test;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class RoleRequestConditionTest {
  @After
  public void tearDown() {
    SecurityContextHolder.clearContext();
  }

  @Test
  public void shouldCombineRoles() {

    RoleRequestCondition roleRequestCondition = new RoleRequestCondition("role1");
    RoleRequestCondition other = new RoleRequestCondition("role2");

    RoleRequestCondition result = roleRequestCondition.combine(other);

    assertEquals(new HashSet<>(Arrays.asList("role1", "role2")), result.getRoles());
  }

  @Test
  public void shouldReturnConditionIfRolesMatch() {
    String role = "test-role";
    HttpServletRequest request = mock(HttpServletRequest.class);

    setAuthenticatedRole(role);

    RoleRequestCondition roleRequestCondition = new RoleRequestCondition(role);
    assertEquals(roleRequestCondition, roleRequestCondition.getMatchingCondition(request));
  }

  @Test
  public void shouldReturnNullIfRolesDoNotMatch() {
    String role = "test-role";
    HttpServletRequest request = mock(HttpServletRequest.class);

    setAuthenticatedRole(role);

    RoleRequestCondition roleRequestCondition = new RoleRequestCondition("other role");

    assertNull(roleRequestCondition.getMatchingCondition(request));
  }

  @Test
  public void shouldCompareCondtionsByNumberOfRoles() {
    assertEquals(
        2,
        new RoleRequestCondition("role1", "role2", "role3")
            .compareTo(new RoleRequestCondition("role1"), mock(HttpServletRequest.class)));

    assertEquals(
        -2,
        new RoleRequestCondition("role1")
            .compareTo(
                new RoleRequestCondition("role1", "role2", "role3"),
                mock(HttpServletRequest.class)));
  }

  private void setAuthenticatedRole(String role) {
    SecurityContext securityContext = mock(SecurityContext.class);
    Authentication authentication = mock(Authentication.class);

    GrantedAuthority grantedAuthority = () -> role;
    Collection<GrantedAuthority> authorities = Collections.singletonList(grantedAuthority);

    when(securityContext.getAuthentication()).thenReturn(authentication);
    doReturn(authorities).when(authentication).getAuthorities();

    SecurityContextHolder.setContext(securityContext);
  }
}
