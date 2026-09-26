package com.piedraazul.personas.infraestructura.persistencia;

import com.piedraazul.personas.aplicacion.puertos.salida.PacienteRepository;
import com.piedraazul.personas.dominio.Paciente;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class PacienteRepositoryAdapter implements PacienteRepository {

    private final SpringDataPacienteJpaRepository jpa;

    public PacienteRepositoryAdapter(SpringDataPacienteJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Paciente guardar(Paciente paciente) {
        PacienteJpaEntity saved = jpa.save(toJpa(paciente));
        return toDomain(saved);
    }

    @Override
    public Optional<Paciente> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Paciente> buscarPorUsuarioId(Long usuarioId) {
        return jpa.findByUsuarioId(usuarioId).map(this::toDomain);
    }

    @Override
    public List<Paciente> listarTodos() {
        return jpa.findAll().stream().map(this::toDomain).toList();
    }

    private Paciente toDomain(PacienteJpaEntity e) {
        return Paciente.reconstituir(e.getId(), e.getUsuarioId(), e.getNombreCompleto(), e.getTelefono());
    }

    private PacienteJpaEntity toJpa(Paciente p) {
        return new PacienteJpaEntity(p.getId(), p.getUsuarioId(), p.getNombreCompleto(), p.getTelefono());
    }
}
