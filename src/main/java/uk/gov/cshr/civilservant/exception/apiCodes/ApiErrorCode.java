package uk.gov.cshr.civilservant.exception.apiCodes;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiErrorCode {

    private String code;
    private String description;
    private int statusCode;
}
