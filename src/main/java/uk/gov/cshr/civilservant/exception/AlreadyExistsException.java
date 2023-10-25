package uk.gov.cshr.civilservant.exception;

import uk.gov.cshr.civilservant.exception.apiCodes.ApiCode;

public class AlreadyExistsException extends CodedHttpException {
    public AlreadyExistsException(String message, ApiCode apiCode) {
        super(message, apiCode);
    }
}
