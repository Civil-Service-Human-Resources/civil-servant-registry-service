package uk.gov.cshr.civilservant.domain;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import uk.gov.cshr.civilservant.exception.apiCodes.ApiCode;

import java.util.ArrayList;
import java.util.List;

import static java.util.Collections.sort;

@Component
public class ErrorDtoFactory {
  public ErrorDto create(HttpStatus httpStatus, List<String> errors) {
    sort(errors);
    ErrorDto errorDto = new ErrorDto();
    errorDto.setStatus(httpStatus.value());
    errorDto.setMessage(httpStatus.getReasonPhrase());
    errorDto.setErrors(new ArrayList<>(errors));
    return errorDto;
  }

  public ErrorDto create(List<String> errors, ApiCode code) {
    HttpStatus status = HttpStatus.valueOf(code.getCode().getStatusCode());
    sort(errors);
    ErrorDto errorDto = new ErrorDto();
    errorDto.setStatus(status.value());
    errorDto.setMessage(status.getReasonPhrase());
    errorDto.setErrors(new ArrayList<>(errors));
    errorDto.setApiErrorCode(code.getCode());
    return errorDto;
  }
}
