package uk.gov.cshr.civilservant.exception.organisationalUnit;

import uk.gov.cshr.civilservant.exception.CodedHttpException;
import uk.gov.cshr.civilservant.exception.apiCodes.ApiCode;

public class InvalidDomainException extends CodedHttpException {
    public InvalidDomainException(String message) {
        super(message, ApiCode.OU002);
    }
}
