package com.macronnect.api_macronnect.venta;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface VentaRepository extends JpaRepository<Venta, Long> {

    /**
     * Carga una venta con su cliente en la misma query (para el listado sin N+1).
     * Los detalles quedan LAZY: en el listado no se necesitan.
     */
    @Override
    @EntityGraph(attributePaths = "cliente")
    Page<Venta> findAll(Pageable pageable);

    /**
     * Trae una venta con cliente Y detalles (con su artículo) en una sola consulta,
     * para el endpoint de detalle. El DISTINCT evita duplicados por el JOIN a la colección.
     */
    @EntityGraph(attributePaths = {"cliente", "detalles", "detalles.articulo"})
    @Query("SELECT v FROM Venta v WHERE v.id = :id")
    Optional<Venta> findByIdConDetalles(@Param("id") Long id);

    /**
     * Filtro combinable por cliente y por rango de fechas (extra elegido).
     * Los parámetros nulos se ignoran gracias a la condición (:param IS NULL OR ...).
     */
    @EntityGraph(attributePaths = "cliente")
    @Query("SELECT v FROM Venta v WHERE "
            + "(:clienteId IS NULL OR v.cliente.id = :clienteId) AND "
            + "(:desde IS NULL OR v.fechaCreacion >= :desde) AND "
            + "(:hasta IS NULL OR v.fechaCreacion <= :hasta)")
    Page<Venta> buscarConFiltros(@Param("clienteId") Long clienteId,
    @Param("desde") LocalDateTime desde,
    @Param("hasta") LocalDateTime hasta,
    Pageable pageable);
}