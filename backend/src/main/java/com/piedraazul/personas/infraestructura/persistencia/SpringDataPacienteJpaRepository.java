package com.piedraazul.personas.infraestructura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

interface SpringDataPacienteJpaRepository extends JpaRepository<PacienteJpaEntity, Long> {
    Optional<PacienteJpaEntity> findByUsuarioId(Long usuarioId);
}
