package uk.gov.cshr.civilservant.exception.apiCodes;

public enum ApiCode {
    OU001(new ApiErrorCode("OU001", "Domain already exists on the organisational unit", 400)),
    OU002(new ApiErrorCode("OU002", "Invalid domain format. Correct format is: 'example.gov.uk'", 400)),

    CS001(new ApiErrorCode("CS001", "Civil servant email domain does not match organisation", 400));

    private final ApiErrorCode code;

    ApiCode(ApiErrorCode code) {
        this.code = code;
    }

    public ApiErrorCode getCode() {
        return code;
    }
}
