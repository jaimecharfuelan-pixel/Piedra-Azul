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
| `/` | Landing pública |
| `/login` / `/registro` | JWT y alta de paciente |
| `/panel/*` | Panel por rol |
| `/agenda` | Citas del día (agendador / médico) |
| `/agendar` | Reservar franja libre |
| `/configuracion` | Ventana (admin) y horarios |
| `/calendario` | FullCalendar por médico |
| `/historial` | Consultas atendidas |
| `/personas` | Catálogo; alta de médicos solo admin |

## Autenticación y permisos en UI

- Token Bearer en `AuthSessionStore` + interceptor HTTP.
- Navegación y botones filtrados por `domain/auth/permisos.ts` (alineado a
  `SecurityConfig`, sin ofrecer acciones que el backend deniega).
- El médico opera solo su `personaId` (agenda, calendario, horario).

## Cómo se conecta con los módulos

- **Identidad**: `POST /api/auth/login`, `POST /api/auth/registro-paciente`
- **Disponibilidad**: `GET/PUT /api/disponibilidad/*`
- **Citas**: `POST/PUT/GET /api/citas/*`
- **Personas**: `GET/POST /api/personas/*`

El algoritmo de slots vive en el backend (`CalculadorSlotsPorIntervaloFijo`).
El frontend solo pinta las franjas que ya vienen libres.

## Usabilidad

- Acciones destructivas pasan por diálogo de confirmación.
- Errores de negocio se muestran con mensaje y sugerencia.
- Estados vacíos indican el siguiente paso.
- La tabla de agenda ordena al pulsar el encabezado.
