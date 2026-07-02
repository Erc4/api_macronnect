package com.macronnect.api_macronnect.common.exception;

public class InsufficientStockException extends BusinessException {

    public InsufficientStockException(String message) {
        super(message);
    }

    public InsufficientStockException(String articulo, int disponible, int solicitado) {
        super(String.format("Stock insuficiente para '%s': disponible %d, solicitado %d",
                articulo, disponible, solicitado));
    }
}