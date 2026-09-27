package com.piedraazul.citas.infraestructura.persistencia;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface SpringDataConsultaJpaRepository extends JpaRepository<ConsultaJpaEntity, Long> {

    List<ConsultaJpaEntity> findByPacienteIdOrderByFechaDescIdDesc(Long pacienteId);

    List<ConsultaJpaEntity> findByMedicoIdOrderByFechaDescIdDesc(Long medicoId);

    Optional<ConsultaJpaEntity> findByCitaId(Long citaId);
}
