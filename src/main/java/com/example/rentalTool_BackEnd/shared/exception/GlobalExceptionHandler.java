package com.example.rentalTool_BackEnd.shared.exception;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.util.HashMap;
import java.util.Map;

import static org.springframework.http.HttpStatus.BAD_REQUEST;

@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<HttpResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.status(BAD_REQUEST).body(
                HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(BAD_REQUEST.value())
                        .httpStatus(BAD_REQUEST)
                        .message(e.getMessage())
                        .reason("Invalid argument")
                        .build()
        );
    }

    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<HttpResponse> handleConstraintViolationException(ConstraintViolationException e) {
        Map<String, String> validationErrors = new HashMap<>();
        
        for (ConstraintViolation<?> violation : e.getConstraintViolations()) {
            String fieldName = violation.getPropertyPath().toString();
            String errorMessage = violation.getMessage();
            validationErrors.put(fieldName, errorMessage);
        }

        return ResponseEntity.status(BAD_REQUEST).body(
                HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(BAD_REQUEST.value())
                        .httpStatus(BAD_REQUEST)
                        .message("Validation failed")
                        .reason("Constraint violation")
                        .data(Map.of("validationErrors", validationErrors))
                        .build()
        );
    }
}

