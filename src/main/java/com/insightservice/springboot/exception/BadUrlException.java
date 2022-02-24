package com.insightservice.springboot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadUrlException extends RuntimeException
{
    public BadUrlException(String message)
    {
        super(message);
    }
}
