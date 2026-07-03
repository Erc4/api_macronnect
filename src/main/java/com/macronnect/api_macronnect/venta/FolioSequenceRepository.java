package com.macronnect.api_macronnect.venta;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import javax.persistence.LockModeType;
import java.util.Optional;

public interface FolioSequenceRepository extends JpaRepository<FolioSequence, String> {

    /**
     * Trae la fila del contador bloqueándola (SELECT ... FOR UPDATE) para que
     * dos ventas concurrentes no obtengan el mismo folio. Debe usarse dentro
     * de la transacción de la venta.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT f FROM FolioSequence f WHERE f.seqName = :seqName")
    Optional<FolioSequence> findBySeqNameForUpdate(@Param("seqName") String seqName);
}