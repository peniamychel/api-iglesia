package com.mcmm.exception;

import com.mcmm.model.payload.ApiResponseException;
import org.springframework.dao.DataAccessException;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;

import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Object> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception, WebRequest webRequest) {
        Map<String, String> errors = new HashMap<>();
        exception.getBindingResult().getAllErrors().forEach((error) -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });
        ApiResponseException apiResponse = ApiResponseException.builder()
                .message(errors)
                .url(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }
    @ExceptionHandler(NotFoundExceptionResource.class)
    public ResponseEntity<ApiResponseException> handleResourceNotFoundException(NotFoundExceptionResource exception, WebRequest webRequest) {
        //ApiResponse apiResponse = new ApiResponse(exception.getMessage(), webRequest.getDescription(false));
        ApiResponseException apiResponse = ApiResponseException.builder()
                .message(exception.getMessage())
                .url(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.NOT_FOUND);
    }


    @ExceptionHandler(BadRequestException.class)
    public ResponseEntity<ApiResponseException> handleBadRequestException(BadRequestException exception, WebRequest webRequest) {
        //ApiResponse apiResponse = new ApiResponse(exception.getMessage(), webRequest.getDescription(false));
        ApiResponseException apiResponse = ApiResponseException.builder()
                .message(exception.getMessage())
                .url(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.BAD_REQUEST);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseException> handlerException(Exception exception,
                                                                 WebRequest webRequest) {
        ApiResponseException apiResponse = ApiResponseException.builder()
                .message(exception.getMessage())
                .url(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponseException> handleDatabaseExceptions(DataAccessException exception, WebRequest webRequest) {
        ApiResponseException apiResponse = ApiResponseException.builder()
                .message(exception.getMessage())
                .url(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(InternalServerErrorExceptionResource.class)
    public ResponseEntity<ApiResponseException>internalServerErrorExceptionResource(DataAccessException exception, WebRequest webRequest){
        ApiResponseException apiResponse = ApiResponseException.builder()
                .message(exception.getMessage())
                .url(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    // Manejo de errores relacionados con la base de datos
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<String> handleDatabaseExceptions(DataIntegrityViolationException ex) {
        return new ResponseEntity<>("Error: Violación de restricciones en la base de datos.", HttpStatus.CONFLICT);
    }

}
