package com.macronnect.api_macronnect.common.exception;

public class ResourceNotFoundException extends RuntimeException {

    public ResourceNotFoundException(String message) {
        super(message);
    }

    public ResourceNotFoundException(String recurso, String campo, Object valor) {
        super(String.format("%s no encontrado con %s = '%s'", recurso, campo, valor));
    }
}