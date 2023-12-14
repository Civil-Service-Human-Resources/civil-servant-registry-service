package uk.gov.cshr.civilservant.exception.civilServant;

import uk.gov.cshr.civilservant.exception.CodedHttpException;
import uk.gov.cshr.civilservant.exception.apiCodes.ApiCode;

public class InvalidUserOrganisationException extends CodedHttpException {
    public InvalidUserOrganisationException(String message) {
        super(message, ApiCode.CS001);
    }
}
