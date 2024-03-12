package uk.gov.cshr.civilservant.controller;

import org.hibernate.exception.ConstraintViolationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import uk.gov.cshr.civilservant.domain.ErrorDto;
import uk.gov.cshr.civilservant.domain.ErrorDtoFactory;
import uk.gov.cshr.civilservant.exception.AlreadyExistsException;
import uk.gov.cshr.civilservant.exception.CodedHttpException;
import uk.gov.cshr.civilservant.exception.NotFoundException;

import java.util.Collections;

@ControllerAdvice
public class ApiExceptionHandler {
  private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

  private final ErrorDtoFactory errorDtoFactory;

  public ApiExceptionHandler(ErrorDtoFactory errorDtoFactory) {
    this.errorDtoFactory = errorDtoFactory;
  }

  @ExceptionHandler(ConstraintViolationException.class)
  protected ResponseEntity<ErrorDto> handleConstraintViolationException(
      ConstraintViolationException e) {
    LOGGER.error("Bad Request: ", e);

    ErrorDto error =
        errorDtoFactory.create(HttpStatus.BAD_REQUEST, Collections.singletonList("Storage error"));

    return ResponseEntity.badRequest().body(error);
  }

  @ExceptionHandler(NotFoundException.class)
  protected ResponseEntity<ErrorDto> handleNotFoundException(NotFoundException e) {
    LOGGER.error("Resource not found: ", e);
    ErrorDto error =
            errorDtoFactory.create(HttpStatus.NOT_FOUND, Collections.singletonList("Resource not found"));
    return ResponseEntity.status(error.getStatus()).body(error);
  }

  @ExceptionHandler(AlreadyExistsException.class)
  protected ResponseEntity<ErrorDto> handleAlreadyExistsException(AlreadyExistsException e) {
    LOGGER.error("Resource already exists: ", e);
    ErrorDto error =
            errorDtoFactory.create(Collections.singletonList("Resource already exists"), e.getApiCode());
    return ResponseEntity.status(error.getStatus()).body(error);
  }

  @ExceptionHandler(CodedHttpException.class)
  protected ResponseEntity<ErrorDto> handleAlreadyExistsException(CodedHttpException e) {
    LOGGER.error("HTTP exception ", e);
    ErrorDto error =
            errorDtoFactory.create(Collections.singletonList(e.getMessage()), e.getApiCode());
    return ResponseEntity.status(error.getStatus()).body(error);
  }
}
