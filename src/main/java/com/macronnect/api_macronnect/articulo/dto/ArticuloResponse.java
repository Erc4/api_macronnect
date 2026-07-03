package com.macronnect.api_macronnect.articulo.dto;

import com.macronnect.api_macronnect.articulo.Articulo;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public class ArticuloResponse {

    private Long id;
    private String codigo;
    private String nombre;
    private String descripcion;
    private BigDecimal precio;
    private Integer stock;
    private Long categoriaId;
    private String categoriaNombre;
    private LocalDateTime fechaCreacion;

    public ArticuloResponse(Long id, String codigo, String nombre, String descripcion,
                            BigDecimal precio, Integer stock,
                            Long categoriaId, String categoriaNombre,
                            LocalDateTime fechaCreacion) {
        this.id = id;
        this.codigo = codigo;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.precio = precio;
        this.stock = stock;
        this.categoriaId = categoriaId;
        this.categoriaNombre = categoriaNombre;
        this.fechaCreacion = fechaCreacion;
    }

    /** Mapea de entidad a DTO. Debe invocarse dentro de la transacción (accede a la categoría LAZY). */
    public static ArticuloResponse fromEntity(Articulo a) {
        return new ArticuloResponse(
                a.getId(),
                a.getCodigo(),
                a.getNombre(),
                a.getDescripcion(),
                a.getPrecio(),
                a.getStock(),
                a.getCategoria().getId(),
                a.getCategoria().getNombre(),
                a.getFechaCreacion());
    }

    public Long getId() { return id; }
    public String getCodigo() { return codigo; }
    public String getNombre() { return nombre; }
    public String getDescripcion() { return descripcion; }
    public BigDecimal getPrecio() { return precio; }
    public Integer getStock() { return stock; }
    public Long getCategoriaId() { return categoriaId; }
    public String getCategoriaNombre() { return categoriaNombre; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}