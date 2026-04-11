package com.mcmm.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(value = HttpStatus.NOT_FOUND)
public class NotFoundExceptionResource extends RuntimeException{

    private String resourceName;
    private String fieldName;
    private Object fieldValue;

    public NotFoundExceptionResource(String resourceName, String fieldName, Object fieldValue) {
        super(String.format("%s no fue encontrado con: %s = '%s'", resourceName, fieldName, fieldValue));
        this.resourceName = resourceName;
        this.fieldName = fieldName;
        this.fieldValue = fieldValue;
    }

    public NotFoundExceptionResource(String resourceName) {
        super(String.format("No hay registoros de '%s'", resourceName));
        this.resourceName = resourceName;
    }

}
