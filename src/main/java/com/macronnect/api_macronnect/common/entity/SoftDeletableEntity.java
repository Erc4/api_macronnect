package com.macronnect.api_macronnect.common.entity;

import javax.persistence.Column;
import javax.persistence.MappedSuperclass;

@MappedSuperclass
public abstract class SoftDeletableEntity extends BaseEntity {

    @Column(nullable = false)
    private Boolean activo = true;

    public Boolean getActivo() {
        return activo;
    }

    public void setActivo(Boolean activo) {
        this.activo = activo;
    }
}