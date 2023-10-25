package uk.gov.cshr.civilservant.exception;

import lombok.Data;
import lombok.EqualsAndHashCode;
import uk.gov.cshr.civilservant.exception.apiCodes.ApiCode;

/**
 * Base exception for raising known HTTP errors
 */
@EqualsAndHashCode(callSuper = true)
@Data
public class CodedHttpException extends RuntimeException {

    private ApiCode apiCode;
    public CodedHttpException(String message, ApiCode apiCode) {
        super(message);
        this.apiCode = apiCode;
    }
}
