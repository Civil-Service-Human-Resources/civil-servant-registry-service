package uk.gov.cshr.civilservant.exception.organisationalUnit;

import uk.gov.cshr.civilservant.exception.AlreadyExistsException;
import uk.gov.cshr.civilservant.exception.apiCodes.ApiCode;

public class DomainAlreadyExistsException extends AlreadyExistsException {
    public DomainAlreadyExistsException(String message) {
        super(message, ApiCode.OU001);
    }
}
