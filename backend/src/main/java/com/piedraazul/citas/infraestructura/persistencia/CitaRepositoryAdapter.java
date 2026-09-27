package com.piedraazul.citas.infraestructura.persistencia;

import com.piedraazul.citas.aplicacion.puertos.salida.CitaRepository;
import com.piedraazul.citas.dominio.Cita;
import com.piedraazul.citas.dominio.OrdenCitas;
import com.piedraazul.nucleo.dominio.EstadoCita;
import com.piedraazul.nucleo.dominio.TimeRange;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class CitaRepositoryAdapter implements CitaRepository {

    private final SpringDataCitaJpaRepository jpa;

    public CitaRepositoryAdapter(SpringDataCitaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Cita guardar(Cita cita) {
        CitaJpaEntity entity = cita.getId() == null
                ? new CitaJpaEntity()
                : jpa.findById(cita.getId()).orElseGet(CitaJpaEntity::new);

        entity.setPacienteId(cita.getPacienteId());
        entity.setMedicoId(cita.getMedicoId());
        entity.setFecha(cita.getFecha());
        entity.setRango(cita.getRango());
        entity.setEstado(cita.getEstado());

        return toDomain(jpa.save(entity));
    }

    @Override
    public Optional<Cita> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Cita> buscarPorMedicoYFecha(Long medicoId, LocalDate fecha) {
        return buscarPorMedicoYFecha(medicoId, fecha, OrdenCitas.POR_DEFECTO);
    }

    @Override
    public List<Cita> buscarPorMedicoYFecha(Long medicoId, LocalDate fecha, OrdenCitas orden) {
        return jpa.findByMedicoIdAndFecha(medicoId, fecha, aSort(orden)).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Cita> buscarProgramadasPorMedicoYFecha(Long medicoId, LocalDate fecha) {
        return jpa.findByMedicoIdAndFechaAndEstado(
                        medicoId,
                        fecha,
                        EstadoCita.PROGRAMADA,
                        aSort(OrdenCitas.POR_DEFECTO)
                ).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Cita> buscarPorMedicoEntreFechas(Long medicoId, LocalDate desde, LocalDate hasta) {
        return jpa.findEntreFechas(medicoId, desde, hasta).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Cita> buscarPorPaciente(Long pacienteId) {
        return jpa.findPorPaciente(pacienteId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public boolean existeSolapamiento(Long medicoId, LocalDate fecha, TimeRange rango) {
        return jpa.contarSolapadas(
                medicoId,
                fecha,
                EstadoCita.PROGRAMADA,
                rango.getHoraInicio(),
                rango.getHoraFin()
        ) > 0;
    }

    @Override
    public boolean existeSolapamiento(Long medicoId, LocalDate fecha, TimeRange rango, Long citaIdExcluida) {
        if (citaIdExcluida == null) {
            return existeSolapamiento(medicoId, fecha, rango);
        }
        return jpa.contarSolapadasExcluyendo(
                medicoId,
                fecha,
                EstadoCita.PROGRAMADA,
                rango.getHoraInicio(),
                rango.getHoraFin(),
                citaIdExcluida
        ) > 0;
    }

    /**
     * Traduce el criterio del RF1 a un ORDER BY real. El desempate por hora
     * mantiene el resultado estable entre peticiones.
     */
    private static Sort aSort(OrdenCitas orden) {
        Sort.Direction direccion = orden.esDescendente() ? Sort.Direction.DESC : Sort.Direction.ASC;
        if (orden.ordenaPorEstado()) {
            return Sort.by(direccion, "estado").and(Sort.by(Sort.Direction.ASC, "rango.horaInicio"));
        }
        // PACIENTE_* se reordena en el servicio (el nombre vive en el módulo Personas):
        // aquí basta con un orden determinista por hora.
        if (orden.ordenaPorPaciente()) {
            return Sort.by(Sort.Direction.ASC, "rango.horaInicio");
        }
        return Sort.by(direccion, "rango.horaInicio").and(Sort.by(Sort.Direction.ASC, "id"));
    }

    private Cita toDomain(CitaJpaEntity entity) {
        return Cita.reconstituir(
                entity.getId(),
                entity.getPacienteId(),
                entity.getMedicoId(),
                entity.getFecha(),
                entity.getRango(),
                entity.getEstado()
        );
    }
}
