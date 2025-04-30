package com.example.rentalTool_BackEnd.tool.exception;

public class ToolNotFoundException extends RuntimeException{
    public ToolNotFoundException(String message) {
        super(message);
    }
}
