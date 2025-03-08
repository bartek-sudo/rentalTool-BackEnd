package com.example.rentalTool_BackEnd.user.web.exception;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.shared.util.TimeUtil;
import com.example.rentalTool_BackEnd.user.exception.IllegalAccountAccessException;
import com.example.rentalTool_BackEnd.user.exception.UserAlreadyExistException;
import com.example.rentalTool_BackEnd.user.exception.UserNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
class UserExceptionHandler {

    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<HttpResponse> handleUserNotFoundException(UserNotFoundException e) {
        return ResponseEntity.status(NOT_FOUND).body(
                HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(NOT_FOUND.value())
                        .httpStatus(NOT_FOUND)
                        .message(e.getMessage())
                        .reason("User not found")
                        .build()
        );
    }

    @ExceptionHandler(UserAlreadyExistException.class)
    public ResponseEntity<HttpResponse> handleUserAlreadyExistException(UserAlreadyExistException e) {
        return ResponseEntity.status(BAD_REQUEST).body(
                HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(BAD_REQUEST.value())
                        .httpStatus(BAD_REQUEST)
                        .message(e.getMessage())
                        .reason("User already exist")
                        .build()
        );
    }

    @ExceptionHandler(IllegalAccountAccessException.class)
    public ResponseEntity<HttpResponse> handleIllegalAccountAccessException(IllegalAccountAccessException e) {
        return ResponseEntity.status(BAD_REQUEST).body(
                HttpResponse.builder()
                        .timeStamp(TimeUtil.getCurrentTimeWithFormat())
                        .statusCode(BAD_REQUEST.value())
                        .httpStatus(BAD_REQUEST)
                        .message(e.getMessage())
                        .reason("Illegal account access")
                        .build()
        );
    }



}
