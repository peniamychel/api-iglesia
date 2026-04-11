package com.mcmm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class InternalServerErrorExceptionResource extends RuntimeException{
    public InternalServerErrorExceptionResource(String resourceName) {
        super(String.format("Error de sevidor: '%s'", resourceName));
    }
}
