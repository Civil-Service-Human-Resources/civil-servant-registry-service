package uk.gov.cshr.civilservant.validation.domain;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {ValidDomainValidator.class})
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidDomain {

    String message() default "Email domain is not in the correct format";
    Class<?>[] groups() default { };

    Class<? extends Payload>[] payload() default { };
}
