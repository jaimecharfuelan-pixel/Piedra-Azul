# Módulo Personas

CRUD de **Especialidad**, **Médico** y consulta de **Paciente**.
Corresponde a `diagramas-por-modulo/03_modulo_personas.puml`.

## Requisitos funcionales

| RF | Rol | Uso en Personas |
|---|---|---|
| RF1 | Agendador | Lista médicos activos para filtrar citas |
| RF2 | Paciente / Médico | Paciente ligado a `usuarioId`; médico en catálogo de slots |
| RF3 | Administrador | Especialidades y médicos activos (base de disponibilidad) |

## Arquitectura hexagonal

```
com.piedraazul.personas
├── dominio/                 # Especialidad, Medico, Paciente
├── aplicacion/
│   ├── puertos/entrada/     # Use cases
│   ├── puertos/salida/      # Repositories + CatalogoMedicosPort + RegistrarPersonaPort
│   └── servicio/            # Services + PersonasModuleFacade
└── infraestructura/
    ├── dto/
    ├── persistencia/        # JPA + Adapters
    └── rest/                # Controllers
```

Depende solo de **`nucleo`** (`RolUsuario`, excepciones). No importa Identidad ni Citas.

### Puertos públicos (otros módulos)

- `CatalogoMedicosPort` → Disponibilidad / Citas
- `RegistrarPersonaPort` → Identidad (al crear usuario MEDICO/PACIENTE)

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
| GET | `/api/personas/pacientes` | Listar |
| GET | `/api/personas/pacientes/{id}` | Buscar por id |

## Base de datos (PostgreSQL / JPA)

| Tabla | Columnas principales |
|---|---|
| `especialidades` | `id`, `nombre`, `activa` |
| `medicos` | `id`, `nombre_completo`, `especialidad_id`, `activo` |
| `pacientes` | `id`, `usuario_id` (unique), `nombre_completo`, `telefono` |

Esquema creado por Hibernate (`ddl-auto=update`). Datos de prueba opcionales en `db/init/`.
