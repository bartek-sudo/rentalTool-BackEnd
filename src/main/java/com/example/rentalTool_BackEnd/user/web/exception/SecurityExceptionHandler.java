package com.example.rentalTool_BackEnd.user.web.exception;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import com.example.rentalTool_BackEnd.user.security.exception.EmailNotVerifiedException;
import com.example.rentalTool_BackEnd.user.security.exception.InvalidCredentialsException;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.DisabledException;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.FORBIDDEN;
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

    @ExceptionHandler(EmailNotVerifiedException.class)
    public ResponseEntity<HttpResponse> handleEmailNotVerifiedException(EmailNotVerifiedException e) {
        return ResponseEntity.status(FORBIDDEN).body(
                HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(FORBIDDEN.value())
                        .httpStatus(FORBIDDEN)
                        .message(e.getMessage())
                        .reason("Email not verified")
                        .build()
        );
    }

    @ExceptionHandler(DisabledException.class)
    public ResponseEntity<HttpResponse> handleDisabledException(DisabledException e) {
        String message = e.getMessage();
        if (message != null && message.contains("disabled") || message != null && message.contains("enabled")) {
            return ResponseEntity.status(FORBIDDEN).body(
                    HttpResponse.builder()
                            .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                            .statusCode(FORBIDDEN.value())
                            .httpStatus(FORBIDDEN)
                            .message("Email address is not verified. Please check your email and verify your account.")
                            .reason("Email not verified")
                            .build()
            );
        }
        return ResponseEntity.status(FORBIDDEN).body(
                HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(FORBIDDEN.value())
                        .httpStatus(FORBIDDEN)
                        .message("Account is disabled")
                        .reason("Account disabled")
                        .build()
        );
    }
}
