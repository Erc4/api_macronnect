package com.macronnect.api_macronnect.cliente.dto;

import com.macronnect.api_macronnect.cliente.Cliente;

import java.time.LocalDateTime;

public class ClienteResponse {

    private Long id;
    private String nombre;
    private String email;
    private String telefono;
    private String direccion;
    private LocalDateTime fechaCreacion;

    public ClienteResponse(Long id, String nombre, String email, String telefono,
        String direccion, LocalDateTime fechaCreacion) {
        this.id = id;
        this.nombre = nombre;
        this.email = email;
        this.telefono = telefono;
        this.direccion = direccion;
        this.fechaCreacion = fechaCreacion;
    }

    public static ClienteResponse fromEntity(Cliente c) {
        return new ClienteResponse(
                c.getId(),
                c.getNombre(),
                c.getEmail(),
                c.getTelefono(),
                c.getDireccion(),
                c.getFechaCreacion());
    }

    public Long getId() { return id; }
    public String getNombre() { return nombre; }
    public String getEmail() { return email; }
    public String getTelefono() { return telefono; }
    public String getDireccion() { return direccion; }
    public LocalDateTime getFechaCreacion() { return fechaCreacion; }
}