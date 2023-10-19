package uk.gov.cshr.civilservant.validation.domain;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import uk.gov.cshr.civilservant.exception.organisationalUnit.InvalidDomainException;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.regex.Pattern;

@Component
public class ValidDomainValidator  implements ConstraintValidator<ValidDomain, String>  {

    @Value("${domains.validation.regex}")
    protected String regexPattern;

    @Override
    public boolean isValid(String value, ConstraintValidatorContext context) {
        if (!Pattern.compile(regexPattern)
                .matcher(value)
                .matches()) {
            throw new InvalidDomainException(String.format("Domain '%s' is not valid", value));
        } else {
            return true;
        }
    }
}
