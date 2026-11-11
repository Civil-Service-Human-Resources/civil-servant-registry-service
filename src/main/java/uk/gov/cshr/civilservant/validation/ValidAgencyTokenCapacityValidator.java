package uk.gov.cshr.civilservant.validation;

import org.hibernate.validator.internal.engine.constraintvalidation.ConstraintValidatorContextImpl;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

@Component
public class ValidAgencyTokenCapacityValidator implements ConstraintValidator<ValidCapacity, Integer> {

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
        } else {
            ((ConstraintValidatorContextImpl) context).addMessageParameter("minValue", minValue);
            ((ConstraintValidatorContextImpl) context).addMessageParameter("maxValue", maxValue);
            return false;
        }
    }
}
