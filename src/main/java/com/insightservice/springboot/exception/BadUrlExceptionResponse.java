package com.insightservice.springboot.exception;

public class BadUrlExceptionResponse
{
    private String message;

    public BadUrlExceptionResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}