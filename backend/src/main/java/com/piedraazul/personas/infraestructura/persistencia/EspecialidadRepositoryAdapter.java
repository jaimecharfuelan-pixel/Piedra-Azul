package com.piedraazul.personas.infraestructura.persistencia;

import com.piedraazul.personas.aplicacion.puertos.salida.EspecialidadRepository;
import com.piedraazul.personas.dominio.Especialidad;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public class EspecialidadRepositoryAdapter implements EspecialidadRepository {

    private final SpringDataEspecialidadJpaRepository jpa;

    public EspecialidadRepositoryAdapter(SpringDataEspecialidadJpaRepository jpa) {
        this.jpa = jpa;
    }

    @Override
    public Especialidad guardar(Especialidad especialidad) {
        EspecialidadJpaEntity saved = jpa.save(toJpa(especialidad));
        return toDomain(saved);
    }

    @Override
    public Optional<Especialidad> buscarPorId(Long id) {
        return jpa.findById(id).map(this::toDomain);
    }

    @Override
    public List<Especialidad> listarActivas() {
        return jpa.findByActivaTrue().stream().map(this::toDomain).toList();
    }

    private Especialidad toDomain(EspecialidadJpaEntity e) {
        return Especialidad.reconstituir(e.getId(), e.getNombre(), e.isActiva());
    }

    private EspecialidadJpaEntity toJpa(Especialidad e) {
        return new EspecialidadJpaEntity(e.getId(), e.getNombre(), e.isActiva());
    }
}
