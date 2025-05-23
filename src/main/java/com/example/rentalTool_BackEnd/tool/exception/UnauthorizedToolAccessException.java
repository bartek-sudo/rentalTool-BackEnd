package com.example.rentalTool_BackEnd.tool.exception;

public class UnauthorizedToolAccessException extends RuntimeException {
    public UnauthorizedToolAccessException(String message) {
        super(message);
    }
}
