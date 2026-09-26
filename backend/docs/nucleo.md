# Núcleo común

Clases y reglas compartidas por **todos** los módulos del monolito modular
PiedraAzul. Corresponde a `diagramas-por-modulo/01_nucleo_comun.puml`.

## Trazabilidad con requisitos funcionales

| Elemento | RF | Uso |
|---|---|---|
| `RolUsuario.AGENDADOR` | RF1 | Listar citas de un médico en una fecha |
| `RolUsuario.PACIENTE` / `MEDICO` | RF2 | Agendar cita web y slots disponibles |
| `RolUsuario.ADMINISTRADOR` | RF3 | Configurar semanas, días, franjas e intervalo |
| `DiaSemana` | RF3 | Días que atiende cada médico/terapeuta |
| `TimeRange` | RF2, RF3 | Franja horaria + duración mín. 30 min / solapes |
| `EstadoCita` | RF1, RF2 | Ciclo de vida de la cita (`ATENDIDA` → Consulta) |

## Contenido

### Enumeraciones

- `DiaSemana` — LUNES … DOMINGO
- `RolUsuario` — ADMINISTRADOR, AGENDADOR, MEDICO, PACIENTE
- `EstadoCita` — PROGRAMADA, CANCELADA, ATENDIDA

### Value Object

- `TimeRange(horaInicio, horaFin)`
  - Rechaza duración &lt; 30 minutos (`DURACION_INVALIDA`)
  - `solapaCon(otro)` para evitar citas solapadas del mismo médico
  - Mapeo JPA `@Embeddable` → columnas `hora_inicio` / `hora_fin` (PostgreSQL `TIME`)

### Excepciones de dominio

Una sola clase por categoría; el **código** distingue el caso:

```java
ReglaDeNegocioException.de("SLOT_NO_DISPONIBLE", "...");
RecursoNoEncontradoException.de("MEDICO_NO_ENCONTRADO", "...");
```

`NucleoExceptionHandler` (`@ControllerAdvice`) mapea a HTTP 400/409/404.

## Paquete Java

```
com.piedraazul.nucleo
├── dominio
│   ├── DiaSemana, RolUsuario, EstadoCita
│   ├── TimeRange
│   └── excepciones/
└── infraestructura.rest
    └── NucleoExceptionHandler
```

No pertenece a un módulo de negocio: **otros módulos dependen de él**, nunca al revés
(desacoplamiento / Dependency Inversion).

## Base de datos

El núcleo no tiene tablas propias. Sus tipos se reutilizan:

| Tipo Java | PostgreSQL |
|---|---|
| Enums (`@Enumerated(STRING)`) | `VARCHAR` |
| `TimeRange` embeddable | `hora_inicio TIME`, `hora_fin TIME` |

Ver también `db/init/01-datos-prueba.sql` (datos de prueba; el esquema lo crea JPA).
