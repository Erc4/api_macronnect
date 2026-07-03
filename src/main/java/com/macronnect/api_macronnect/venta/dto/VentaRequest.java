package com.macronnect.api_macronnect.venta.dto;

import javax.validation.Valid;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.List;

public class VentaRequest {

    @NotNull(message = "El id del cliente es obligatorio")
    private Long clienteId;

    @NotEmpty(message = "La venta debe tener al menos una línea de detalle")
    @Valid
    private List<DetalleVentaRequest> detalles;

    public Long getClienteId() {
        return clienteId;
    }

    public void setClienteId(Long clienteId) {
        this.clienteId = clienteId;
    }

    public List<DetalleVentaRequest> getDetalles() {
        return detalles;
    }

    public void setDetalles(List<DetalleVentaRequest> detalles) {
        this.detalles = detalles;
    }
}