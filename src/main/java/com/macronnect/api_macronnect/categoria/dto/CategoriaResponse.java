package com.macronnect.api_macronnect.categoria.dto;

import com.macronnect.api_macronnect.categoria.Categoria;

import java.time.LocalDateTime;

public class CategoriaResponse {

    private Long id;
    private String nombre;
    private String descripcion;
    private LocalDateTime fechaCreacion;

    public CategoriaResponse(Long id, String nombre, String descripcion,
                             LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.descripcion = descripcion;
        this.fechaCreacion = fechaCreacion;
    }

    /** Mapea de entidad a DTO de respuesta. */
    public static CategoriaResponse fromEntity(Categoria categoria) {
        return new CategoriaResponse(
                categoria.getId(),
                categoria.getNombre(),
                categoria.getDescripcion(),
                categoria.getFechaCreacion());
    }

    public Long getId() {
        return id;
    }

    public String getNombre() {
        return nombre;
    }

    public String getDescripcion() {
        return descripcion;
    }

    public LocalDateTime getFechaCreacion() {
        return fechaCreacion;
    }
}