package com.piedraazul.disponibilidad.infraestructura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

interface SpringDataPeriodoDisponibilidadJpaRepository extends JpaRepository<PeriodoDisponibilidadJpaEntity, Long> {

    /**
     * Periodos que cubren la fecha. Devuelve lista (y no Optional) para que un dato
     * inconsistente no rompa la consulta: el adaptador toma el más reciente.
     */
    @Query("""
            select p from PeriodoDisponibilidadJpaEntity p
            where p.medicoId = :medicoId
              and p.fechaInicio <= :fecha
              and (p.fechaFin is null or p.fechaFin >= :fecha)
            order by p.fechaInicio desc
            """)
    List<PeriodoDisponibilidadJpaEntity> findVigentes(
            @Param("medicoId") Long medicoId,
            @Param("fecha") LocalDate fecha
    );

    Optional<PeriodoDisponibilidadJpaEntity> findFirstByMedicoIdAndFechaFinIsNullOrderByFechaInicioDesc(Long medicoId);

    List<PeriodoDisponibilidadJpaEntity> findByMedicoIdOrderByFechaInicioDesc(Long medicoId);
}
