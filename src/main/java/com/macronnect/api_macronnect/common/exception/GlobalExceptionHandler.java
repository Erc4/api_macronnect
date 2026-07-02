package com.macronnect.api_macronnect.common.exception;

import com.macronnect.api_macronnect.common.dto.ErrorResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import javax.servlet.http.HttpServletRequest;
import javax.validation.ConstraintViolation;
import javax.validation.ConstraintViolationException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@RestControllerAdvice
public class GlobalExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(GlobalExceptionHandler.class);

    // 404 - recurso no encontrado
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                         HttpServletRequest request) {
        return build(HttpStatus.NOT_FOUND, ex.getMessage(), request);
    }

    // 401 - credenciales inválidas en el login
    @ExceptionHandler(BadCredentialsException.class)
    public ResponseEntity<ErrorResponse> handleBadCredentials(BadCredentialsException ex,
                                                              HttpServletRequest request) {
        return build(HttpStatus.UNAUTHORIZED, "Usuario o contraseña incorrectos", request);
    }

    // 409 - duplicado (ej. código de artículo repetido)
    @ExceptionHandler(DuplicateResourceException.class)
    public ResponseEntity<ErrorResponse> handleDuplicate(DuplicateResourceException ex,
                                                          HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // 409 - reglas de negocio (incluye InsufficientStockException)
    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<ErrorResponse> handleBusiness(BusinessException ex,
                                                         HttpServletRequest request) {
        return build(HttpStatus.CONFLICT, ex.getMessage(), request);
    }

    // 400 - validación del @RequestBody (@Valid)
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleBodyValidation(MethodArgumentNotValidException ex,
                                                              HttpServletRequest request) {
        List<ErrorResponse.FieldValidationError> fieldErrors = new ArrayList<>();
        for (FieldError fe : ex.getBindingResult().getFieldErrors()) {
            fieldErrors.add(new ErrorResponse.FieldValidationError(
                    fe.getField(), fe.getDefaultMessage()));
        }
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "La solicitud contiene errores de validación",
                request.getRequestURI());
        body.setFieldErrors(fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    // 400 - validación de @RequestParam / @PathVariable
    @ExceptionHandler(ConstraintViolationException.class)
    public ResponseEntity<ErrorResponse> handleParamValidation(ConstraintViolationException ex,
                                                               HttpServletRequest request) {
        List<ErrorResponse.FieldValidationError> fieldErrors = ex.getConstraintViolations()
                .stream()
                .map(v -> new ErrorResponse.FieldValidationError(campoDe(v), v.getMessage()))
                .collect(Collectors.toList());
        ErrorResponse body = new ErrorResponse(
                HttpStatus.BAD_REQUEST.value(),
                HttpStatus.BAD_REQUEST.getReasonPhrase(),
                "Parámetros inválidos",
                request.getRequestURI());
        body.setFieldErrors(fieldErrors);
        return ResponseEntity.badRequest().body(body);
    }

    // 409 - violación de integridad en BD (respaldo del índice único)
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
        HttpServletRequest request) {
        log.warn("Violación de integridad de datos en {}: {}", request.getRequestURI(), ex.getMessage());
        return build(HttpStatus.CONFLICT,
                "La operación viola una restricción de integridad de datos", request);
    }

    // 500 - cualquier cosa no contemplada (AHORA SÍ loguea la causa real)
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGeneric(Exception ex,
                                                        HttpServletRequest request) {
        log.error("Error inesperado en {}", request.getRequestURI(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR,
                "Ocurrió un error inesperado", request);
    }

    private ResponseEntity<ErrorResponse> build(HttpStatus status, String message,
                                                HttpServletRequest request) {
        ErrorResponse body = new ErrorResponse(
                status.value(),
                status.getReasonPhrase(),
                message,
                request.getRequestURI());
        return ResponseEntity.status(status).body(body);
    }

    private String campoDe(ConstraintViolation<?> v) {
        String path = v.getPropertyPath().toString();
        int i = path.lastIndexOf('.');
        return i >= 0 ? path.substring(i + 1) : path;
    }
}