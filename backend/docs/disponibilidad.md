# Módulo Disponibilidad

Configura **cuándo** se puede agendar y **qué franjas** ofrece cada médico.
Corresponde a `diagramas-por-modulo/04_modulo_disponibilidad.puml`.

## Requisitos funcionales

| RF | Rol | Qué resuelve este módulo |
|---|---|---|
| **RF3** | Administrador / Agendador / Médico | Ventana de agendamiento en semanas, días de atención, franja horaria, duración de la cita y descanso entre citas |
| RF2 | Paciente | Entrega las franjas libres que verá al reservar |
| RF1 | Agendador | Indirectamente: sin horario vigente un médico no tiene agenda |

## El problema difícil: horarios que cambian en el tiempo

Un médico no tiene *un* horario, tiene una **sucesión de horarios**. Por ejemplo
atiende lunes a viernes de 8 a 12 durante dos semanas y desde la tercera pasa a
martes y jueves de 14 a 18.

Cada fila de `periodos_disponibilidad` es un "contrato de horario" válido sólo en
un rango de fechas:

- `fecha_fin = null` significa *vigente hasta que se cree un horario nuevo*.
- Al registrar un periodo que empieza mientras otro sigue abierto, el servicio
  cierra el anterior el día previo (`PeriodoDisponibilidad.cerrarEn`).
- Además se valida que el nuevo rango no se cruce con ningún periodo existente
  del mismo médico (`PERIODO_SOLAPADO`).

Así nunca coexisten dos horarios del mismo profesional, y consultar "¿qué horario
rige el 15 de abril?" es una sola consulta por rango de fechas.

## Arquitectura hexagonal

```
com.piedraazul.disponibilidad
├── dominio/
│   ├── ConfiguracionSistema          # ventana global, una sola fila
│   ├── PeriodoDisponibilidad         # horario vigente en un rango de fechas
│   ├── SlotDisponible                # Value Object (record) fecha + rango
│   ├── CalculadorSlotsStrategy       # Strategy: punto de extensión
│   └── CalculadorSlotsPorIntervaloFijo
├── aplicacion/
│   ├── puertos/entrada/              # ConfigurarVentana…, ConfigurarPeriodo…,
│   │                                 # ConsultarDisponibilidad…, ConsultarPeriodos…
│   ├── puertos/salida/               # DisponibilidadRepository
│   │                                 # ConsultarConfiguracionPort (público)
│   │                                 # PeriodoDisponibilidadRef (vista pública)
│   └── servicio/                     # Services + DisponibilidadModuleFacade
└── infraestructura/
    ├── config/                       # bean de la Strategy
    ├── dto/
    ├── persistencia/                 # JPA + Adapter
    └── rest/                         # DisponibilidadController
```

### Dependencias entre módulos

| Necesita | De quién | Cómo |
|---|---|---|
| `CatalogoMedicosPort` | Personas | Valida que el médico exista y esté activo |
| `ConsultarCitasPort` | Citas | Pregunta qué horas ya están ocupadas |

Y publica hacia Citas:

| Puerto público | Implementado por | Consumidor |
|---|---|---|
| `ConsultarConfiguracionPort` | `DisponibilidadModuleFacade` | Citas |
| `PeriodoDisponibilidadRef` | la entidad `PeriodoDisponibilidad` | Citas |

`PeriodoDisponibilidadRef` es la pieza que evita que Citas importe la entidad de
otro módulo: es una interfaz de sólo lectura con las preguntas que Citas necesita
hacer (`incluyeFecha`, `atiendeEnDia`, `cubreRango`, `coincideConRejilla`).

## El algoritmo de franjas (Strategy)

`CalculadorSlotsPorIntervaloFijo` recorre la franja horaria en pasos de
`duracionCitaMinutos + descansoEntreCitasMinutos` y descarta los pasos que se
cruzan con los rangos ocupados.

!!! note "Por qué se cuenta en minutos y no con `LocalTime.plusMinutes`"
    `LocalTime` da la vuelta a medianoche. Una franja que termina a las 23:30
    generaría un slot 23:30–00:00 que parecería válido. El recorrido se hace con
    enteros (minutos desde medianoche) y así el último slot nunca desborda el día.

Ejemplo con franja 14:00–18:00, cita de 45 min y descanso de 15 min (paso = 60):

| # | Slot |
|---|---|
| 1 | 14:00 – 14:45 |
| 2 | 15:00 – 15:45 |
| 3 | 16:00 – 16:45 |
| 4 | 17:00 – 17:45 |

Cambiar la regla de cálculo (sobrecupos, slots variables) es agregar otra
implementación de `CalculadorSlotsStrategy` y sustituir el bean en
`DisponibilidadBeansConfig`, sin tocar el caso de uso.

## Reglas de negocio y códigos de error

| Código | Cuándo | HTTP |
|---|---|---|
| `VENTANA_AGENDAMIENTO_INVALIDA` | Ventana fuera de 1–52 semanas | 400 |
| `DURACION_CITA_INVALIDA` | Cita de menos de 30 min, o que no cabe en la franja | 400 |
| `DESCANSO_ENTRE_CITAS_INVALIDO` | Descanso fuera de 0–120 min | 400 |
| `PERIODO_SIN_DIAS_ATENCION` | Ningún día seleccionado | 400 |
| `PERIODO_RANGO_FECHAS_INVALIDO` | `fechaFin` anterior a `fechaInicio` | 400 |
| `RANGO_HORARIO_INVALIDO` | `horaFin` no posterior a `horaInicio` (`TimeRange`) | 400 |
| `PERIODO_SOLAPADO` | El nuevo horario se cruza con otro del mismo médico | 409 |
| `MEDICO_NO_DISPONIBLE` | Médico inexistente/inactivo, o sin horario vigente esa fecha | 409 |
| `FUERA_DE_VENTANA_AGENDAMIENTO` | Fecha fuera de `[hoy, hoy + ventana]` | 409 |

## API REST

Prefijo `/api/disponibilidad`.

| Método | Ruta | Descripción |
|---|---|---|
| GET | `/configuracion` | Ventana actual y límites de fecha ya calculados |
| PUT | `/configuracion` | Cambia la ventana (`{"semanas": 4}`) |
| POST | `/periodos` | Registra un horario (RF3); cierra el anterior si aplica |
| GET | `/periodos?medicoId=` | Historial de horarios, del más reciente al más antiguo |
| GET | `/slots?medicoId=&fecha=` | Franjas libres de un día (RF2) |
| GET | `/slots/rango?medicoId=&desde=&hasta=` | Franjas libres de un rango, para el calendario |

### Ejemplo: configurar un horario

```bash
curl -X POST http://localhost:8080/api/disponibilidad/periodos \
  -H 'Content-Type: application/json' \
  -d '{
    "medicoId": 1,
    "fechaInicio": "2026-09-27",
    "diasAtencion": ["LUNES","MARTES","MIERCOLES","JUEVES","VIERNES"],
    "horaInicio": "08:00",
    "horaFin": "12:00",
    "duracionCitaMinutos": 30,
    "descansoEntreCitasMinutos": 0
  }'
```

`fechaFin` es opcional: si no se envía, el periodo queda abierto.

### Formato de fechas y horas

Las horas viajan siempre como `HH:mm` (lo fija `JacksonTiempoConfig`) y las
fechas como `yyyy-MM-dd`. A la entrada también se acepta `HH:mm:ss`.

## Base de datos

| Tabla | Columnas | Notas |
|---|---|---|
| `configuracion_sistema` | `id`, `ventana_semanas` | Id asignado (siempre 1): imposible tener dos filas |
| `periodos_disponibilidad` | `id`, `medico_id`, `fecha_inicio`, `fecha_fin`, `hora_inicio`, `hora_fin`, `duracion_cita_minutos`, `descanso_entre_citas_minutos` | `hora_inicio`/`hora_fin` vienen del `@Embeddable TimeRange` |
| `periodo_dias_atencion` | `periodo_id`, `dia_semana` | Tabla hija normalizada (una fila por día), con FK y unicidad `(periodo_id, dia_semana)` |

Índices: `idx_periodo_medico (medico_id)` y
`idx_periodo_medico_vigencia (medico_id, fecha_inicio, fecha_fin)`, que es
exactamente el acceso de `buscarPeriodoVigente`.

## Extensiones sobre el diagrama

El diagrama se implementó completo. Se añadió lo siguiente porque los RF lo piden
y el diagrama no lo cubría:

| Añadido | Motivo |
|---|---|
| `descansoEntreCitasMinutos` | El RF3 pide "el intervalo de tiempo (minutos) que cada médico tiene entre cita y cita", que es distinto de la duración de la cita |
| `ConsultarPeriodosDisponibilidadUseCase` | El diagrama ya tenía `listarPeriodosPorMedico` en el repositorio; hacía falta exponerlo para que la pantalla de configuración muestre qué está vigente |
| `ConfigurarVentanaAgendamientoUseCase.consultar()` | Precargar el formulario con el valor actual |
| `ConsultarDisponibilidadUseCase.ejecutarPorRango()` | FullCalendar pide semanas completas; sin esto haría siete peticiones |
| `PeriodoDisponibilidadRef` | Cumplir la regla de que un módulo no importa entidades de otro |
