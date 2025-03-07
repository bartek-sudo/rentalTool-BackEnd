package com.example.rentalTool_BackEnd.user.exception;

public class IllegalAccountAccessException extends RuntimeException {
    public IllegalAccountAccessException(String message) {
        super(message);
    }
}
