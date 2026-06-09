package uk.gov.cshr.civilservant.domain;

import lombok.Data;

@Data
public class FieldError {

    private final String field;
    private final String error;

}
