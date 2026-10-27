package com.example.trantoc.exception;

import com.example.trantoc.dto.ApiErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(BusinessException.class)
    @ResponseStatus(HttpStatus.BAD_REQUEST)
    public ApiErrorResponse handleBusinessException(
            BusinessException ex) {

        return new ApiErrorResponse(
                ex.getMessage()
        );
    }
}