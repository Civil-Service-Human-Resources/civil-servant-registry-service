package uk.gov.cshr.civilservant.domain;

import lombok.Data;
import uk.gov.cshr.civilservant.exception.apiCodes.ApiErrorCode;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

@Data
public class ErrorDto {
  private final Instant timestamp = Instant.now();
  private List<String> errors = new ArrayList<>();
  private int status;
  private String message;
  private ApiErrorCode apiErrorCode;
}
