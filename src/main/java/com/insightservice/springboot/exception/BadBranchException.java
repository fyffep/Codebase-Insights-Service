package com.insightservice.springboot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.BAD_REQUEST)
public class BadBranchException extends RuntimeException
{
    public BadBranchException(String message)
    {
        super(message);
    }
}
