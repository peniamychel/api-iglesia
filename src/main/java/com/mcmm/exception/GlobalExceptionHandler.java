package com.mcmm.exception;

import com.mcmm.model.payload.ApiResponseException;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private com.mcmm.model.dao.BitacoraDao bitacoraDao;

    @Autowired
    private com.mcmm.model.dao.UsuarioDao usuarioDao;

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

    private String registrarErrorEnBitacora(Exception exception, WebRequest webRequest) {
        String errorCode = "ERR-" + java.util.UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        
        String username = "ANONYMOUS";
        com.mcmm.model.entity.Usuario usuario = null;
        try {
            org.springframework.security.core.Authentication authentication = 
                org.springframework.security.core.context.SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated() && !"anonymousUser".equals(authentication.getName())) {
                username = authentication.getName();
                usuario = usuarioDao.findByUsername(username).orElse(null);
            }
        } catch (Exception e) {
            System.err.println("Error al recuperar usuario para bitacora: " + e.getMessage());
        }

        String ip = "";
        try {
            if (webRequest instanceof org.springframework.web.context.request.ServletWebRequest) {
                jakarta.servlet.http.HttpServletRequest servletRequest = 
                    ((org.springframework.web.context.request.ServletWebRequest) webRequest).getRequest();
                ip = servletRequest.getRemoteAddr();
            }
        } catch (Exception e) {
            System.err.println("Error al recuperar IP para bitacora: " + e.getMessage());
        }

        String fullErrorMessage = exception.getMessage();
        if (fullErrorMessage == null) {
            fullErrorMessage = exception.getClass().getName();
        }
        
        java.io.StringWriter sw = new java.io.StringWriter();
        java.io.PrintWriter pw = new java.io.PrintWriter(sw);
        exception.printStackTrace(pw);
        String stackTrace = sw.toString();
        
        String descripcion = String.format("Code: %s | Msg: %s | Stack: %s", 
            errorCode, 
            fullErrorMessage, 
            stackTrace
        );

        com.mcmm.model.entity.Bitacora log = com.mcmm.model.entity.Bitacora.builder()
                .usuario(usuario)
                .username(username)
                .accion("ERROR_SISTEMA")
                .modulo("ERROR")
                .descripcion(descripcion.substring(0, Math.min(descripcion.length(), 1000)))
                .ipAddress(ip)
                .fecha(java.time.LocalDateTime.now())
                .build();
        
        try {
            bitacoraDao.save(log);
        } catch (Exception ex) {
            System.err.println("Error al persistir bitacora de error: " + ex.getMessage());
        }

        return errorCode;
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<ApiResponseException> handlerException(Exception exception,
                                                                 WebRequest webRequest) {
        String errorCode = registrarErrorEnBitacora(exception, webRequest);
        String userFriendlyMessage = String.format("Error interno del servidor (Código: %s). Si el problema persiste, por favor contacte con soporte técnico.", errorCode);
        ApiResponseException apiResponse = ApiResponseException.builder()
                .message(userFriendlyMessage)
                .url(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(DataAccessException.class)
    public ResponseEntity<ApiResponseException> handleDatabaseExceptions(DataAccessException exception, WebRequest webRequest) {
        String errorCode = registrarErrorEnBitacora(exception, webRequest);
        String userFriendlyMessage = String.format("Error de base de datos (Código: %s). Si el problema persiste, por favor contacte con soporte técnico.", errorCode);
        ApiResponseException apiResponse = ApiResponseException.builder()
                .message(userFriendlyMessage)
                .url(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.SERVICE_UNAVAILABLE);
    }

    @ExceptionHandler(InternalServerErrorExceptionResource.class)
    public ResponseEntity<ApiResponseException> internalServerErrorExceptionResource(InternalServerErrorExceptionResource exception, WebRequest webRequest){
        String errorCode = registrarErrorEnBitacora(exception, webRequest);
        String userFriendlyMessage = String.format("Error interno del servidor (Código: %s). Si el problema persiste, por favor contacte con soporte técnico.", errorCode);
        ApiResponseException apiResponse = ApiResponseException.builder()
                .message(userFriendlyMessage)
                .url(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiResponseException> handleDatabaseExceptions(DataIntegrityViolationException exception, WebRequest webRequest) {
        String errorCode = registrarErrorEnBitacora(exception, webRequest);
        String userFriendlyMessage = String.format("Violación de restricciones en la base de datos (Código: %s). Si el problema persiste, por favor contacte con soporte técnico.", errorCode);
        ApiResponseException apiResponse = ApiResponseException.builder()
                .message(userFriendlyMessage)
                .url(webRequest.getDescription(false))
                .build();
        return new ResponseEntity<>(apiResponse, HttpStatus.CONFLICT);
    }
}
