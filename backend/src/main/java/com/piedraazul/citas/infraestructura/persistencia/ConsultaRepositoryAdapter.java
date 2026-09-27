package com.piedraazul.citas.infraestructura.persistencia;

import com.piedraazul.citas.aplicacion.puertos.salida.ConsultaRepository;
import com.piedraazul.citas.dominio.Consulta;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class ConsultaRepositoryAdapter implements ConsultaRepository {

    private final SpringDataConsultaJpaRepository jpa;

    public ConsultaRepositoryAdapter(SpringDataConsultaJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Consulta guardar(Consulta consulta) {
        ConsultaJpaEntity entity = consulta.getId() == null
                ? new ConsultaJpaEntity()
                : jpa.findById(consulta.getId()).orElseGet(ConsultaJpaEntity::new);

        entity.setCitaId(consulta.getCitaId());
        entity.setMedicoId(consulta.getMedicoId());
        entity.setPacienteId(consulta.getPacienteId());
        entity.setFecha(consulta.getFecha());
        entity.setObservaciones(consulta.getObservaciones());
        entity.setFechaRegistro(consulta.getFechaRegistro());

        return toDomain(jpa.save(entity));
    }

    @Override
    public List<Consulta> listarPorPaciente(Long pacienteId) {
        return jpa.findByPacienteIdOrderByFechaDescIdDesc(pacienteId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public List<Consulta> listarPorMedico(Long medicoId) {
        return jpa.findByMedicoIdOrderByFechaDescIdDesc(medicoId).stream()
                .map(this::toDomain)
                .toList();
    }

    @Override
    public Optional<Consulta> buscarPorCitaId(Long citaId) {
        return jpa.findByCitaId(citaId).map(this::toDomain);
    }

    private Consulta toDomain(ConsultaJpaEntity entity) {
        return Consulta.reconstituir(
                entity.getId(),
                entity.getCitaId(),
                entity.getMedicoId(),
                entity.getPacienteId(),
                entity.getFecha(),
                entity.getObservaciones(),
                entity.getFechaRegistro()
        );
    }
}
