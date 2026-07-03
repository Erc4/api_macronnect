package com.macronnect.api_macronnect.articulo;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface ArticuloRepository extends JpaRepository<Articulo, Long> {

    boolean existsByCodigo(String codigo);

    boolean existsByCodigoAndIdNot(String codigo, Long id);

    /**
     * Sobrescribe el findAll paginado para traer la categoría en la misma query
     * (JOIN) y evitar el problema N+1 al mapear cada artículo a su DTO.
     */
    @Override
    @EntityGraph(attributePaths = "categoria")
    Page<Articulo> findAll(Pageable pageable);

    /**
     * Busca un artículo bloqueando su fila para escritura (SELECT ... FOR UPDATE).
     * Se usará DENTRO de la transacción de la venta para descontar stock de forma
     * segura ante concurrencia. DEBE llamarse dentro de un método @Transactional.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT a FROM Articulo a WHERE a.id = :id")
    Optional<Articulo> findByIdForUpdate(@Param("id") Long id);
}