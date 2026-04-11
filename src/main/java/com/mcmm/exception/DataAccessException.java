package com.mcmm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.INTERNAL_SERVER_ERROR)
public class DataAccessException extends RuntimeException{

    public DataAccessException(String message) {
        super(message);
    }
}
