# PiedraAzul — Backend

Monolito modular con **Spring Boot**, **Spring Security**, **JWT** y documentación con **MkDocs + mkdocstrings**.

## Stack

- Spring Boot 3.4 / Java 17
- Spring Security (stateless) + JWT (`jjwt`)
- Spring Data JPA + **PostgreSQL** (Docker) / H2 (local)
- MkDocs Material + mkdocstrings (documentación)

> **FullCalendar** se usa en el **frontend** Angular para renderizar la agenda;
> este backend expone los endpoints de citas/disponibilidad que alimentan el calendario.

## Perfiles

| Perfil | Uso | Base de datos |
|---|---|---|
| `local` (default) | `mvn spring-boot:run` | H2 en memoria |
| `docker` | contenedor / Compose | PostgreSQL |

## Ejecutar la API (local)

```bash
mvn spring-boot:run
```

## Docker

Desde la raíz del monorepo:

```bash
docker compose up --build
```

Ver `docker-compose.yml` y `db/init/` para datos de prueba SQL.