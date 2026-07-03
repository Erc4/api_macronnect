package com.macronnect.api_macronnect.venta.dto;

import com.macronnect.api_macronnect.venta.Venta;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public class VentaResponse {

    private Long id;
    private Long folio;
    private LocalDateTime fecha;
    private Long clienteId;
    private String clienteNombre;
    private String estado;
    private BigDecimal total;
    private List<DetalleVentaResponse> detalles;

    public VentaResponse(Long id, Long folio, LocalDateTime fecha, Long clienteId,
        String clienteNombre, String estado, BigDecimal total,
        List<DetalleVentaResponse> detalles) {
        this.id = id;
        this.folio = folio;
        this.fecha = fecha;
        this.clienteId = clienteId;
        this.clienteNombre = clienteNombre;
        this.estado = estado;
        this.total = total;
        this.detalles = detalles;
    }

    /** Versión completa: incluye las líneas de detalle (para crear, ver detalle, cancelar). */
    public static VentaResponse fromEntity(Venta v) {
        List<DetalleVentaResponse> lineas = v.getDetalles().stream()
                .map(DetalleVentaResponse::fromEntity)
                .collect(Collectors.toList());
        return new VentaResponse(
                v.getId(), v.getFolio(), v.getFechaCreacion(),
                v.getCliente().getId(), v.getCliente().getNombre(),
                v.getEstado().name(), v.getTotal(), lineas);
    }

    /** Versión resumen: sin líneas de detalle (para el listado). */
    public static VentaResponse fromEntityResumen(Venta v) {
        return new VentaResponse(
                v.getId(), v.getFolio(), v.getFechaCreacion(),
                v.getCliente().getId(), v.getCliente().getNombre(),
                v.getEstado().name(), v.getTotal(), null);
    }

    public Long getId() { return id; }
    public Long getFolio() { return folio; }
    public LocalDateTime getFecha() { return fecha; }
    public Long getClienteId() { return clienteId; }
    public String getClienteNombre() { return clienteNombre; }
    public String getEstado() { return estado; }
    public BigDecimal getTotal() { return total; }
    public List<DetalleVentaResponse> getDetalles() { return detalles; }
}