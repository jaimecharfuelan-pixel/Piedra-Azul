# PiedraAzul

Sistema de agendamiento de citas médicas y de terapia. Proyecto de Ingeniería de
Software III, organizado en **dos carpetas** (dos repos Git) más orquestación
Docker en la raíz.

| Carpeta | Stack | Qué incluye |
|---|---|---|
| [`backend/`](backend/) | Spring Boot 3.4 + Security + JWT + JPA | API en monolito modular |
| [`frontend/`](frontend/) | Angular 19 hexagonal + FullCalendar | Interfaz y calendario |
| [`db/init/`](db/init/) | SQL | Scripts opcionales de arranque de Postgres |
| [`scripts/`](scripts/) | PowerShell | Prueba de humo de los tres RF |
| [`diagramas-por-modulo/`](diagramas-por-modulo/) | PlantUML | Diagramas de clases por módulo |

## Arrancar (3 contenedores)

```bash
docker compose up --build -d
```

| Servicio | URL / puerto |
|---|---|
| Frontend | <http://localhost:4200> |
| Backend API | <http://localhost:8080> |
| Health del backend | <http://localhost:8080/actuator/health> |
| PostgreSQL | localhost:5432 (`piedraazul` / `piedraazul`) |

El frontend espera a que el backend responda `UP` antes de arrancar.

La primera vez que el backend encuentra la base vacía siembra datos de
demostración: dos especialidades, **Dra. Ana Pérez** y **Ft. Carlos Muñoz** con
horario configurado, dos pacientes y un par de citas de ejemplo. Se desactiva con
`APP_SEED_ENABLED=false` en `docker-compose.yml`.

```bash
docker compose down -v   # borra también el volumen de Postgres
```

## Estado de los módulos

| Módulo | Requisitos | Estado |
|---|---|---|
| Núcleo común (01) | RF1–RF3 | Implementado |
| Identidad (02) | RF2 | **Pendiente** — sin login, la API de negocio está abierta |
| Personas (03) | RF1–RF3 | Implementado |
| Disponibilidad (04) | RF3, RF2 | Implementado |
| Citas (05) | RF1, RF2 | Implementado |

## Requisitos funcionales y dónde probarlos

| RF | Descripción | Pantalla | API |
|---|---|---|---|
| **RF1** | El agendador lista las citas de un médico en una fecha y ve la cantidad | `/agenda` | `GET /api/citas?medicoId=&fecha=&orden=` |
| **RF2** | El paciente agenda por la web viendo las franjas disponibles | `/agendar` | `GET /api/disponibilidad/slots`, `POST /api/citas` |
| **RF3** | El administrador configura ventana, días, franja, duración y descanso | `/configuracion` | `PUT /api/disponibilidad/configuracion`, `POST /api/disponibilidad/periodos` |

Guía paso a paso: [`GUIA_PRUEBAS_MANUALES.md`](GUIA_PRUEBAS_MANUALES.md).

## Pruebas

```bash
# Unitarias + integración del backend (H2 en memoria, sin Docker)
cd backend && mvn test

# Humo de los tres RF contra los contenedores en marcha
powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1
```

## Documentación

```bash
cd backend
pip install -r requirements-docs.txt
mkdocs serve      # http://127.0.0.1:8000
```

## Desarrollo sin Docker

```bash
# Backend con H2 en memoria (perfil "local", el de por defecto)
cd backend && mvn spring-boot:run

# Frontend con proxy hacia localhost:8080
cd frontend && npm install && npm start    # http://localhost:4200
```
