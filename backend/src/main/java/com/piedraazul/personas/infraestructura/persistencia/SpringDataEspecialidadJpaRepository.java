package com.piedraazul.personas.infraestructura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface SpringDataEspecialidadJpaRepository extends JpaRepository<EspecialidadJpaEntity, Long> {
    List<EspecialidadJpaEntity> findByActivaTrue();
}
