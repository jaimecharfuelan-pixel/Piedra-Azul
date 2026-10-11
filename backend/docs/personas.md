# Personas

Módulo personas: especialidades, médicos y pacientes.
Diagrama: `diagramas-por-modulo/03_modulo_personas.puml`.

El alta de paciente con cuenta va por Identidad (`registro-paciente`). Acá
también hay walk-in (`POST /api/personas/pacientes`) para agendador/médico.
Por cédula (RF5, lectura): `GET .../pacientes/cedula/{cedula}` — agendador y médico.

## Requisitos funcionales

| RF | Rol | Uso en Personas |
|---|---|---|
| RF1 | Agendador | Lista médicos activos para filtrar citas |
| RF2 | Paciente / Médico | Paciente ligado a `usuarioId`; médico en catálogo de slots |
| RF3 | Administrador | Especialidades y médicos activos (base de disponibilidad) |

## Capas

Dominio: `Especialidad`, `Medico`, `Paciente`.  
Aplicación: use cases + `PersonasModuleFacade`.  
Infra: REST, JPA, DTOs.

Depende solo de `nucleo`. Diagrama: [Personas](diagramas/03-personas.md).

Puertos que publicamos: `CatalogoMedicosPort`, `CatalogoPacientesPort`,
`RegistrarPersonaPort`.

## API REST

| Método | Ruta | Descripción |
|---|---|---|
| POST | `/api/personas/especialidades` | Crear especialidad |
| PUT | `/api/personas/especialidades/{id}/estado` | Activar/desactivar |
| GET | `/api/personas/especialidades` | Listar activas |
| POST | `/api/personas/medicos` | Crear médico |
| PUT | `/api/personas/medicos/{id}` | Actualizar |
| PUT | `/api/personas/medicos/{id}/estado` | Activar/desactivar |
| GET | `/api/personas/medicos` | Listar activos |
| GET | `/api/personas/medicos/{id}` | Buscar por id |
| POST | `/api/personas/pacientes` | walk-in |
| GET | `/api/personas/pacientes` | listar |
| GET | `/api/personas/pacientes/{id}` | por id |
| GET | `/api/personas/pacientes/cedula/{cedula}` | RF5 (agendador/médico) |

## Base de datos (PostgreSQL / JPA)

| Tabla | Columnas principales |
|---|---|
| `especialidades` | `id`, `nombre`, `activa` |
| `medicos` | `id`, `nombre_completo`, `especialidad_id`, `activo` |
| `pacientes` | `id`, `usuario_id` (unique), `nombre_completo`, `telefono` |

Esquema creado por Hibernate (`ddl-auto=update`). Datos de prueba opcionales en `db/init/`.
