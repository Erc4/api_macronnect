package com.macronnect.api_macronnect.common.exception;

public class DuplicateResourceException extends RuntimeException {

    public DuplicateResourceException(String message) {
        super(message);
    }

    public DuplicateResourceException(String recurso, String campo, Object valor) {
        super(String.format("Ya existe %s con %s = '%s'", recurso, campo, valor));
    }
}