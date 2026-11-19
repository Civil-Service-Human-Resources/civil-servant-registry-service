package uk.gov.cshr.civilservant.validation;

import org.hibernate.validator.constraintvalidation.HibernateConstraintValidatorContext;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class ValidAgencyTokenCapacityValidator
        implements ConstraintValidator<ValidCapacity, Integer> {

    @Value("${agencyToken.capacity.min}")
    protected int minValue;

    @Value("${agencyToken.capacity.max}")
    protected int maxValue;

    @Override
    public void initialize(ValidCapacity constraintAnnotation) {
    }

    @Override
    public boolean isValid(Integer value, ConstraintValidatorContext context) {
        if (value != null && value >= minValue && value <= maxValue) {
            return true;
        }

        HibernateConstraintValidatorContext hibernateContext =
                context.unwrap(HibernateConstraintValidatorContext.class);

        hibernateContext.addMessageParameter("minValue", minValue);
        hibernateContext.addMessageParameter("maxValue", maxValue);

        return false;
    }
}