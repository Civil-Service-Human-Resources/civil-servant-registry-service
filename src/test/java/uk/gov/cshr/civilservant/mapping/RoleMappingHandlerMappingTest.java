package uk.gov.cshr.civilservant.mapping;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

import java.lang.reflect.Method;
import java.security.Principal;

import org.junit.Test;

import org.springframework.core.annotation.AnnotationUtils;
import uk.gov.cshr.civilservant.controller.ReportController;

public class RoleMappingHandlerMappingTest {

  private final RoleMappingHandlerMapping handlerMapping =
          new RoleMappingHandlerMapping();

  @Test
  public void shouldReturnNullIfAnnotationNotFoundOnClass() {
    Class<?> type = UnannotatedController.class;

    assertNull(handlerMapping.getCustomTypeCondition(type));
  }

  @Test
  public void shouldReturnRoleRequestConditionForMethodAnnotation() throws Exception {
    Method method = AnnotatedController.class.getMethod("annotatedMethod");

    RoleMapping annotation =
            AnnotationUtils.findAnnotation(method, RoleMapping.class);

    assertEquals(
            new RoleRequestCondition(annotation.value()),
            handlerMapping.getCustomMethodCondition(method)
    );
  }

  @Test
  public void shouldReturnNullIfAnnotationNotFoundOnMethod() throws Exception {
    Method method = UnannotatedController.class.getMethod("unannotatedMethod");

    assertNull(handlerMapping.getCustomMethodCondition(method));
  }

  private static class UnannotatedController {
    public void unannotatedMethod() {}
  }

  private static class AnnotatedController {
    @RoleMapping("test-role")
    public void annotatedMethod() {}
  }
}