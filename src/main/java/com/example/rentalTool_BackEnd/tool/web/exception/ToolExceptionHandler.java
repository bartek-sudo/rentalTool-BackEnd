package com.example.rentalTool_BackEnd.tool.web.exception;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.tool.exception.ToolNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
class ToolExceptionHandler {

    @ExceptionHandler(ToolNotFoundException.class)
    public ResponseEntity<HttpResponse> handleToolNotFoundException(ToolNotFoundException e) {
        return ResponseEntity.status(NOT_FOUND).body(HttpResponse
                .builder()
                .message(e.getMessage())
                .reason("Tool has not been found")
                .statusCode(NOT_FOUND.value())
                .httpStatus(NOT_FOUND)
                .build());
    }

}
