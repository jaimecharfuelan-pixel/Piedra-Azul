-- Quita el catálogo que dejaban las pruebas de humo antiguas
-- (Dr. Humo, especialidad Prueba, Paciente A/B). No toca los datos de
-- demostración (Dra. Ana Pérez, Ft. Carlos Muñoz, Juan Ramírez, María Gómez).

DELETE FROM consultas WHERE cita_id IN (
  SELECT id FROM citas WHERE medico_id IN (SELECT id FROM medicos WHERE nombre_completo LIKE 'Dr. Humo %')
     OR paciente_id IN (
          SELECT id FROM pacientes
          WHERE nombre_completo LIKE 'Paciente A %' OR nombre_completo LIKE 'Paciente B %'
     )
);

DELETE FROM citas WHERE medico_id IN (SELECT id FROM medicos WHERE nombre_completo LIKE 'Dr. Humo %')
   OR paciente_id IN (
        SELECT id FROM pacientes
        WHERE nombre_completo LIKE 'Paciente A %' OR nombre_completo LIKE 'Paciente B %'
   );

DELETE FROM periodo_dias_atencion WHERE periodo_id IN (
  SELECT p.id FROM periodos_disponibilidad p
  INNER JOIN medicos m ON m.id = p.medico_id
  WHERE m.nombre_completo LIKE 'Dr. Humo %'
);

DELETE FROM periodos_disponibilidad WHERE medico_id IN (
  SELECT id FROM medicos WHERE nombre_completo LIKE 'Dr. Humo %'
);

DELETE FROM medicos WHERE nombre_completo LIKE 'Dr. Humo %';
DELETE FROM especialidades WHERE nombre LIKE 'Prueba %';
DELETE FROM pacientes
 WHERE nombre_completo LIKE 'Paciente A %' OR nombre_completo LIKE 'Paciente B %';
