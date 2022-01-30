package com.insightservice.springboot.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.servlet.mvc.method.annotation.ResponseEntityExceptionHandler;


/**
 * Converts custom exceptions into ResponseEntity objects.
 * Credit to https://github.com/AgileIntelligence/AgileIntPPMTool/  for the method logic in this class.
 */
@RestController
@ControllerAdvice
public class CustomResponseEntityExceptionHandler extends ResponseEntityExceptionHandler
{
    //Malformed URL
    @ExceptionHandler
    public final ResponseEntity<Object> handleBadUrlException(BadUrlException ex, WebRequest request)
    {
        BadUrlExceptionResponse exceptionResponse = new BadUrlExceptionResponse(ex.getMessage());
        return new ResponseEntity(exceptionResponse, HttpStatus.BAD_REQUEST);
    }
}
