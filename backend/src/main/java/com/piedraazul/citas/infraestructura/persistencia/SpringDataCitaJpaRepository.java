package com.piedraazul.citas.infraestructura.persistencia;

import com.piedraazul.nucleo.dominio.EstadoCita;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

interface SpringDataCitaJpaRepository extends JpaRepository<CitaJpaEntity, Long> {

    /** El orden se recibe como {@link Sort} para que sea la base de datos la que ordene. */
    List<CitaJpaEntity> findByMedicoIdAndFecha(Long medicoId, LocalDate fecha, Sort sort);

    List<CitaJpaEntity> findByMedicoIdAndFechaAndEstado(
            Long medicoId,
            LocalDate fecha,
            EstadoCita estado,
            Sort sort
    );

    @Query("""
            select c from CitaJpaEntity c
            where c.medicoId = :medicoId
              and c.fecha between :desde and :hasta
            order by c.fecha asc, c.rango.horaInicio asc
            """)
    List<CitaJpaEntity> findEntreFechas(
            @Param("medicoId") Long medicoId,
            @Param("desde") LocalDate desde,
            @Param("hasta") LocalDate hasta
    );

    @Query("""
            select c from CitaJpaEntity c
            where c.pacienteId = :pacienteId
            order by c.fecha desc, c.rango.horaInicio desc
            """)
    List<CitaJpaEntity> findPorPaciente(@Param("pacienteId") Long pacienteId);

    /**
     * Cuenta las citas del médico ese día que se cruzan con el rango pedido.
     * Sólo las PROGRAMADA ocupan agenda: al cancelar, la franja se libera.
     */
    @Query("""
            select count(c) from CitaJpaEntity c
            where c.medicoId = :medicoId
              and c.fecha = :fecha
              and c.estado = :estado
              and c.rango.horaInicio < :horaFin
              and c.rango.horaFin > :horaInicio
            """)
    long contarSolapadas(
            @Param("medicoId") Long medicoId,
            @Param("fecha") LocalDate fecha,
            @Param("estado") EstadoCita estado,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin
    );

    @Query("""
            select count(c) from CitaJpaEntity c
            where c.medicoId = :medicoId
              and c.fecha = :fecha
              and c.estado = :estado
              and c.id <> :citaIdExcluida
              and c.rango.horaInicio < :horaFin
              and c.rango.horaFin > :horaInicio
            """)
    long contarSolapadasExcluyendo(
            @Param("medicoId") Long medicoId,
            @Param("fecha") LocalDate fecha,
            @Param("estado") EstadoCita estado,
            @Param("horaInicio") LocalTime horaInicio,
            @Param("horaFin") LocalTime horaFin,
            @Param("citaIdExcluida") Long citaIdExcluida
    );
}
