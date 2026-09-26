package com.piedraazul.personas.infraestructura.persistencia;

import com.piedraazul.personas.aplicacion.puertos.salida.MedicoRepository;
import com.piedraazul.personas.dominio.Medico;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class MedicoRepositoryAdapter implements MedicoRepository {

    private final SpringDataMedicoJpaRepository jpa;

    public MedicoRepositoryAdapter(SpringDataMedicoJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Medico guardar(Medico medico) {
        MedicoJpaEntity saved = jpa.save(toJpa(medico));
        return toDomain(saved);
    }

    @Override
    public Optional<Medico> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Medico> listarActivos() {
        return jpa.findByActivoTrue().stream().map(this::toDomain).toList();
    }

    private Medico toDomain(MedicoJpaEntity e) {
        return Medico.reconstituir(e.getId(), e.getNombreCompleto(), e.getEspecialidadId(), e.isActivo());
    }

    private MedicoJpaEntity toJpa(Medico m) {
        return new MedicoJpaEntity(m.getId(), m.getNombreCompleto(), m.getEspecialidadId(), m.estaActivo());
    }
}
