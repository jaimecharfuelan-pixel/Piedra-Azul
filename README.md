# PiedraAzul

Agendamiento de citas médicas y de terapia (Ingeniería de Software III).

| Carpeta | Contenido |
|---|---|
| [`backend/`](backend/) | API Spring Boot (monolito modular + JWT) |
| [`frontend/`](frontend/) | SPA Angular 19 |
| [`db/init/`](db/init/) | Scripts opcionales al crear el volumen Postgres |
| [`diagramas-por-modulo/`](diagramas-por-modulo/) | PlantUML de implementación |

## Arranque

```bash
docker compose up --build -d
```

| Servicio | URL |
|---|---|
| Frontend | http://localhost:4200 |
| API | http://localhost:8080 |
| Health | http://localhost:8080/actuator/health |
| Documentación | `docker compose up docs` → http://localhost:8000 |
| PostgreSQL | `localhost:5432` (`piedraazul` / `piedraazul`) |

Con `APP_SEED_ENABLED=true` el backend carga usuarios y citas de demo la primera
vez (password `demo1234`). Para reiniciar la base:

```bash
docker compose down -v && docker compose up --build -d
```

## Módulos

| Módulo | Estado |
|---|---|
| Núcleo | Implementado |
| Identidad (JWT / roles) | Implementado |
| Personas | Implementado |
| Disponibilidad | Implementado |
| Citas | Implementado |

## Pruebas

```bash
cd backend && mvn test
```

Documentación detallada (MkDocs): ver sección *Pruebas* en el sitio de docs.
