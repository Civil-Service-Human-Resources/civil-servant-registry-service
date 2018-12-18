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

import java.util.Collections;

@ControllerAdvice
public class ApiExceptionHandler {
    private static final Logger LOGGER = LoggerFactory.getLogger(ApiExceptionHandler.class);

    private final ErrorDtoFactory errorDtoFactory;

    public ApiExceptionHandler(ErrorDtoFactory errorDtoFactory) {
        this.errorDtoFactory = errorDtoFactory;
    }

    @ExceptionHandler(ConstraintViolationException.class)
    protected ResponseEntity<ErrorDto> handleConstraintViolationException(ConstraintViolationException e) {
        LOGGER.error("Bad Request: ", e);

        ErrorDto error = errorDtoFactory.create(HttpStatus.BAD_REQUEST, Collections.singletonList("Storage error"));

        return ResponseEntity.badRequest().body(error);
    }
}