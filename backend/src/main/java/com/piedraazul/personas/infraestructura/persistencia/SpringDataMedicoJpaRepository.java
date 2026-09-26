package com.piedraazul.personas.infraestructura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

interface SpringDataMedicoJpaRepository extends JpaRepository<MedicoJpaEntity, Long> {
    List<MedicoJpaEntity> findByActivoTrue();
}
