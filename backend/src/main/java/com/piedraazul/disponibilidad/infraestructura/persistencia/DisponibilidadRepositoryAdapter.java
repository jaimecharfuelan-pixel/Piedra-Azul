package com.piedraazul.disponibilidad.infraestructura.persistencia;

import com.piedraazul.disponibilidad.aplicacion.puertos.salida.DisponibilidadRepository;
import com.piedraazul.disponibilidad.dominio.ConfiguracionSistema;
import com.piedraazul.disponibilidad.dominio.PeriodoDisponibilidad;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public class DisponibilidadRepositoryAdapter implements DisponibilidadRepository {

    private final SpringDataConfiguracionSistemaJpaRepository configuracionJpa;
    private final SpringDataPeriodoDisponibilidadJpaRepository periodoJpa;

    public DisponibilidadRepositoryAdapter(
            SpringDataConfiguracionSistemaJpaRepository configuracionJpa,
            SpringDataPeriodoDisponibilidadJpaRepository periodoJpa
    ) {
        this.configuracionJpa = configuracionJpa;
        this.periodoJpa = periodoJpa;
    }

    @Override
    public ConfiguracionSistema obtenerConfiguracionSistema() {
        return configuracionJpa.findById(ConfiguracionSistema.ID_UNICO)
                .map(entity -> ConfiguracionSistema.reconstituir(entity.getId(), entity.getVentanaSemanas()))
                .orElseGet(ConfiguracionSistema::porDefecto);
    }

    @Override
    public ConfiguracionSistema guardarConfiguracionSistema(ConfiguracionSistema config) {
        ConfiguracionSistemaJpaEntity entity = configuracionJpa.findById(ConfiguracionSistema.ID_UNICO)
                .orElseGet(() -> new ConfiguracionSistemaJpaEntity(
                        ConfiguracionSistema.ID_UNICO,
                        config.getVentanaSemanas()
                ));
        entity.setVentanaSemanas(config.getVentanaSemanas());
        ConfiguracionSistemaJpaEntity guardada = configuracionJpa.save(entity);
        return ConfiguracionSistema.reconstituir(guardada.getId(), guardada.getVentanaSemanas());
    }

    @Override
    public PeriodoDisponibilidad guardarPeriodo(PeriodoDisponibilidad periodo) {
        PeriodoDisponibilidadJpaEntity entity = periodo.getId() == null
                ? new PeriodoDisponibilidadJpaEntity()
                : periodoJpa.findById(periodo.getId()).orElseGet(PeriodoDisponibilidadJpaEntity::new);

        entity.setMedicoId(periodo.getMedicoId());
        entity.setFechaInicio(periodo.getFechaInicio());
        entity.setFechaFin(periodo.getFechaFin());
        entity.setDiasAtencion(periodo.getDiasAtencion());
        entity.setFranjaHoraria(periodo.getFranjaHoraria());
        entity.setDuracionCitaMinutos(periodo.getDuracionCitaMinutos());
        entity.setDescansoEntreCitasMinutos(periodo.getDescansoEntreCitasMinutos());

        return toDomain(periodoJpa.save(entity));
    }

    @Override
    public Optional<PeriodoDisponibilidad> buscarPeriodoVigente(Long medicoId, LocalDate fecha) {
        return periodoJpa.findVigentes(medicoId, fecha).stream()
                .findFirst()
                .map(this::toDomain);
    }

    @Override
    public Optional<PeriodoDisponibilidad> buscarPeriodoAbiertoAnterior(Long medicoId) {
        return periodoJpa.findFirstByMedicoIdAndFechaFinIsNullOrderByFechaInicioDesc(medicoId)
                .map(this::toDomain);
    }

    @Override
    public List<PeriodoDisponibilidad> listarPeriodosPorMedico(Long medicoId) {
        return periodoJpa.findByMedicoIdOrderByFechaInicioDesc(medicoId).stream()
                .map(this::toDomain)
                .toList();
    }

    private PeriodoDisponibilidad toDomain(PeriodoDisponibilidadJpaEntity entity) {
        return PeriodoDisponibilidad.reconstituir(
                entity.getId(),
                entity.getMedicoId(),
                entity.getFechaInicio(),
                entity.getFechaFin(),
                entity.getDiasAtencion(),
                entity.getFranjaHoraria(),
                entity.getDuracionCitaMinutos(),
                entity.getDescansoEntreCitasMinutos()
        );
    }
}
