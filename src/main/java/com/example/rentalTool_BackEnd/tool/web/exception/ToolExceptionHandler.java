package com.example.rentalTool_BackEnd.tool.web.exception;

import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import com.example.rentalTool_BackEnd.tool.exception.ImageNotFoundException;
import com.example.rentalTool_BackEnd.tool.exception.InvalidFileTypeException;
import com.example.rentalTool_BackEnd.tool.exception.ToolNotFoundException;
import com.example.rentalTool_BackEnd.tool.exception.UnauthorizedToolAccessException;
import com.example.rentalTool_BackEnd.tool.terms.exception.TermsNotFoundException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.FORBIDDEN;
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

    @ExceptionHandler(ImageNotFoundException.class)
    public ResponseEntity<HttpResponse> handleImageNotFoundException(ImageNotFoundException e) {
        return ResponseEntity.status(NOT_FOUND).body(HttpResponse
                .builder()
                .message(e.getMessage())
                .reason("Image has not been found")
                .statusCode(NOT_FOUND.value())
                .httpStatus(NOT_FOUND)
                .build());
    }

    @ExceptionHandler(UnauthorizedToolAccessException.class)
    public ResponseEntity<HttpResponse> handleUnauthorizedToolAccessException(UnauthorizedToolAccessException e) {
        return ResponseEntity.status(FORBIDDEN).body(HttpResponse
                .builder()
                .message(e.getMessage())
                .reason("Unauthorized access to tool")
                .statusCode(FORBIDDEN.value())
                .httpStatus(FORBIDDEN)
                .build());
    }

    @ExceptionHandler(InvalidFileTypeException.class)
    public ResponseEntity<HttpResponse> handleInvalidFileType(InvalidFileTypeException e) {
        return ResponseEntity.status(BAD_REQUEST).body(HttpResponse
                .builder()
                .message(e.getMessage())
                .reason("Invalid file type")
                .statusCode(BAD_REQUEST.value())
                .httpStatus(BAD_REQUEST)
                .build());
    }

    @ExceptionHandler(TermsNotFoundException.class)
    public ResponseEntity<HttpResponse> handleTermsNotFoundException(TermsNotFoundException e) {
        return ResponseEntity.status(NOT_FOUND).body(HttpResponse
                .builder()
                .message(e.getMessage())
                .reason("Terms has not been found")
                .statusCode(NOT_FOUND.value())
                .httpStatus(NOT_FOUND)
                .build());
    }

}
