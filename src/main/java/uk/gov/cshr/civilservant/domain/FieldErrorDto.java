package uk.gov.cshr.civilservant.domain;

import lombok.Data;
import uk.gov.cshr.civilservant.exception.apiCodes.ApiErrorCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class FieldErrorDto {
  private final Instant timestamp = Instant.now();
  private List<FieldError> errors = new ArrayList<>();
  private int status;
  private String message;
  private ApiErrorCode apiErrorCode;
}
