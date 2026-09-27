# PiedraAzul — Frontend

Angular 19 con **arquitectura hexagonal** y **FullCalendar** para la agenda.

## Stack

| Tecnología | Rol |
|---|---|
| Angular 19 (standalone) | UI |
| Arquitectura hexagonal | Dominio / aplicación / adaptadores |
| FullCalendar 7 (`@fullcalendar/angular`) | Calendario de citas |
| HttpClient | Adaptador hacia el backend Spring Boot |

## Capas

```
src/app/
├── domain/           # Modelos y puertos (sin Angular HTTP ni UI)
│   ├── models/
│   └── ports/
├── application/      # Casos de uso
│   └── use-cases/
├── infrastructure/   # Adaptadores secundarios
│   ├── http/         # REST → backend
│   └── calendar/     # Mapeo a eventos FullCalendar
├── presentation/     # Adaptadores primarios (páginas / componentes)
│   └── pages/
└── shared/
```

La inversión de dependencias se cablea en `app.config.ts`:

```ts
{ provide: CitaRepositoryPort, useClass: CitaHttpAdapter }
```

## Ejecutar

```bash
npm install
npm start
```

Abre http://localhost:4200.

| Ruta | Qué hace |
|---|---|
| `/agenda` | Listado del día con filtros, cantidad y orden |
| `/agendar` | Registro, franjas libres y confirmación |
| `/configuracion` | Ventana de reservas, días, franja, duración y descanso |
| `/calendario` | Vista semanal o mensual de las citas |

El backend esperado: `http://localhost:8080`. Documentación MkDocs: `cd backend && mkdocs serve`.

## Git

Esta carpeta es un repositorio independiente listo para `git init` / push.
