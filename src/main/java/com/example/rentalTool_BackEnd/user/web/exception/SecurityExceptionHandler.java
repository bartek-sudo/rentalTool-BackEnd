package com.example.rentalTool_BackEnd.user.web.exception;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import com.example.rentalTool_BackEnd.user.security.exception.InvalidCredentialsException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.UNAUTHORIZED;

@ControllerAdvice
public class SecurityExceptionHandler {

    @ExceptionHandler(InvalidCredentialsException.class)
    public ResponseEntity<HttpResponse> handleInvalidCredentialsException(InvalidCredentialsException e) {
        return ResponseEntity.status(UNAUTHORIZED).body(
                HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(UNAUTHORIZED.value())
                        .httpStatus(UNAUTHORIZED)
                        .message(e.getMessage())
                        .reason("Authorization failed")
                        .build()
        );
    }
}
