package com.macronnect.api_macronnect.venta.dto;

import com.macronnect.api_macronnect.venta.DetalleVenta;

import java.math.BigDecimal;

public class DetalleVentaResponse {

    private Long articuloId;
    private String articuloCodigo;
    private String articuloNombre;
    private Integer cantidad;
    private BigDecimal precioUnitario;
    private BigDecimal subtotal;

    public DetalleVentaResponse(Long articuloId, String articuloCodigo, String articuloNombre,
                                Integer cantidad, BigDecimal precioUnitario, BigDecimal subtotal) {
        this.articuloId = articuloId;
        this.articuloCodigo = articuloCodigo;
        this.articuloNombre = articuloNombre;
        this.cantidad = cantidad;
        this.precioUnitario = precioUnitario;
        this.subtotal = subtotal;
    }

    public static DetalleVentaResponse fromEntity(DetalleVenta d) {
        return new DetalleVentaResponse(
                d.getArticulo().getId(),
                d.getArticulo().getCodigo(),
                d.getArticulo().getNombre(),
                d.getCantidad(),
                d.getPrecioUnitario(),
                d.getSubtotal());
    }

    public Long getArticuloId() { return articuloId; }
    public String getArticuloCodigo() { return articuloCodigo; }
    public String getArticuloNombre() { return articuloNombre; }
    public Integer getCantidad() { return cantidad; }
    public BigDecimal getPrecioUnitario() { return precioUnitario; }
    public BigDecimal getSubtotal() { return subtotal; }
}