package com.insightservice.springboot.exception;

public class BadBranchExceptionResponse
{
    private String message;

    public BadBranchExceptionResponse(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}