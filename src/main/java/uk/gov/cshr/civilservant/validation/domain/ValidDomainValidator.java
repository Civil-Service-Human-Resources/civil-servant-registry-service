package uk.gov.cshr.civilservant.validation.domain;

import uk.gov.cshr.civilservant.exception.organisationalUnit.InvalidDomainException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

public class ValidDomainValidator  implements ConstraintValidator<ValidDomain, String>  {
    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        final String regexPattern = "^[^-][A-Za-z0-9-]+(\\.[A-Za-z0-9-]+)*(\\.[A-Za-z]{2,})$";
        if (!Pattern.compile(regexPattern)
                .matcher(value)
                .matches()) {
            throw new InvalidDomainException(String.format("Domain '%s' is not valid", value));
        } else {
            return true;
        }
    }
}
