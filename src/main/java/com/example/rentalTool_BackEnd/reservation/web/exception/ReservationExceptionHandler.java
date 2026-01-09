package com.example.rentalTool_BackEnd.reservation.web.exception;

import com.example.rentalTool_BackEnd.reservation.exception.ReservationNotFoundException;
import com.example.rentalTool_BackEnd.reservation.exception.ToolNotAvailableException;
import com.example.rentalTool_BackEnd.shared.model.HttpResponse;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import static org.springframework.http.HttpStatus.BAD_REQUEST;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@ControllerAdvice
class ReservationExceptionHandler {

    @ExceptionHandler(ReservationNotFoundException.class)
    public ResponseEntity<HttpResponse> handleReservationNotFoundException(ReservationNotFoundException e) {
        return ResponseEntity.status(NOT_FOUND).body(HttpResponse
                .builder()
                .message(e.getMessage())
                .reason("Reservation has not been found")
                .statusCode(NOT_FOUND.value())
                .httpStatus(NOT_FOUND)
                .build());
    }

    @ExceptionHandler(ToolNotAvailableException.class)
    public ResponseEntity<HttpResponse> handleToolNotAvailableException(ToolNotAvailableException e) {
        return ResponseEntity.status(BAD_REQUEST).body(HttpResponse
                .builder()
                .message(e.getMessage())
                .reason("Tool is not available for the requested period")
                .statusCode(BAD_REQUEST.value())
                .httpStatus(BAD_REQUEST)
                .build());
    }

}
