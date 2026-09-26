# PiedraAzul

Proyecto de Ingeniería de Software III organizado en **dos carpetas** (dos repos Git)
más orquestación Docker en la raíz.

| Carpeta | Stack | Qué incluye |
|---|---|---|
| [`backend/`](backend/) | Spring Boot + Security + JWT + JPA | API monolito modular |
| [`frontend/`](frontend/) | Angular hexagonal + FullCalendar | UI y agenda |
| [`db/init/`](db/init/) | SQL de datos de prueba | Plantilla vacía para INSERTs |
| [`docker-compose.yml`](docker-compose.yml) | Docker Compose | 3 contenedores: frontend, backend, PostgreSQL |

## Docker (3 contenedores)

```bash
docker compose up --build
```

| Servicio | URL / puerto |
|---|---|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8080 |
| PostgreSQL | localhost:5432 (`piedraazul` / `piedraazul`) |

JPA/Hibernate crea el esquema (`ddl-auto=update`). Los scripts en `db/init/`
se ejecutan solo al crear el volumen por primera vez.

```bash
docker compose down -v   # borra también el volumen de Postgres
```
