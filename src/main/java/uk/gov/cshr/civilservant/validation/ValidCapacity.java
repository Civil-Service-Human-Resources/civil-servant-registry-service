package uk.gov.cshr.civilservant.validation;

import javax.validation.Constraint;
import javax.validation.Payload;
import java.lang.annotation.*;

@Documented
@Constraint(validatedBy = {ValidAgencyTokenCapacityValidator.class})
@Target({ElementType.FIELD})
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidCapacity {

    String message() default "{agency.spaces.invalid}";

    Class<?>[] groups() default {};

    Class<? extends Payload>[] payload() default {};
}