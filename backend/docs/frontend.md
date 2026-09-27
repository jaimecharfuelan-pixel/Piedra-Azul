# Frontend (Angular hexagonal)

SPA en Angular 19. Consume la API del monolito modular y cubre los tres
requisitos funcionales con pantallas dedicadas.

## Stack

| Tecnología | Uso |
|---|---|
| Angular 19 (standalone, signals) | UI |
| Arquitectura hexagonal | Dominio → casos de uso → adaptadores HTTP |
| Reactive Forms | Validación en cliente (RF1 filtros, RF2 registro/agendar, RF3 config) |
| FullCalendar 7 (`@fullcalendar/angular`) | Semana de franjas (RF2) y agenda visual |
| Atomic Design | Átomos (`ui-*`), moléculas, organismos (`pz-*`) |
| Heurísticas de Nielsen | Confirmación, estados vacíos, errores recuperables, feedback |

## Capas

```
src/app/
├── domain/            # Modelos y puertos (sin HttpClient ni componentes)
├── application/       # Casos de uso
├── infrastructure/    # CitaHttpAdapter, DisponibilidadHttpAdapter, FullCalendar
└── presentation/      # Páginas y sistema de diseño
```

Los puertos se atan en `app.config.ts`. Cambiar REST por otra fuente es cambiar
esas líneas.

## Pantallas y requisitos

| Ruta | Qué hace |
|---|---|
| `/agenda` | Citas de un médico en una fecha, con cantidad y orden |
| `/agendar` | Registro del paciente, franjas libres y confirmación |
| `/configuracion` | Ventana de reservas y horario de cada médico |
| `/calendario` | Citas del médico en FullCalendar, con color por estado |
| `/historial` | Consultas creadas al marcar una cita como atendida |
| `/personas` | Especialidades, médicos y registro de pacientes |

## Cómo se conecta con los módulos 04 y 05

- **Disponibilidad**: `DisponibilidadRepositoryPort` → `GET/PUT /api/disponibilidad/*`
- **Citas**: `CitaRepositoryPort` → `POST/PUT/GET /api/citas/*`

El algoritmo de slots vive en el backend (`CalculadorSlotsPorIntervaloFijo`).
El frontend solo pinta las franjas que ya vienen libres.

## Usabilidad

- Toda acción destructiva (cancelar, atender) pasa por `pz-confirm-dialog`.
- Los errores de negocio se traducen a un mensaje + una sugerencia
  (`error-dominio.mapper.ts`).
- Los estados vacíos dicen qué hacer después (heurística de ayuda).
- El orden de la tabla del RF1 se cambia pulsando el encabezado (reconocimiento
  antes que recuerdo).
