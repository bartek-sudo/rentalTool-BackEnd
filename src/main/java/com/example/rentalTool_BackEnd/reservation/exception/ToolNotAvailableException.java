package com.example.rentalTool_BackEnd.reservation.exception;

public class ToolNotAvailableException extends RuntimeException {
    public ToolNotAvailableException(String message) {
        super(message);
    }
}
