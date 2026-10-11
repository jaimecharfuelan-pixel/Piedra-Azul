# PiedraAzul

Docs del monolito (MkDocs). Código: Spring Boot + Angular + Keycloak.

## Stack

| Cosa | Uso |
|---|---|
| Spring Boot 3.4 | API |
| Keycloak | JWT (RNF1) |
| JPA + Postgres | persistencia (H2 en local) |
| Angular 19 | SPA |
| PlantUML | diagramas (se renderizan acá) |

## Ver las docs

Se levantan con:

```bash
docker compose up docs
```

http://localhost:8000

## Módulos

| Módulo | Paquete |
|---|---|
| Núcleo | `nucleo` |
| Identidad | `identidad` |
| Personas | `personas` |
| Disponibilidad | `disponibilidad` |
| Citas | `citas` |

[Arquitectura](arquitectura.md) · [Seguridad / Keycloak](seguridad.md) · [Diagramas](diagramas/index.md)

## Compose

| Servicio | URL |
|---|---|
| front | http://localhost:4200 |
| API | http://localhost:8080 |
| Keycloak | http://localhost:8180 |
| docs | http://localhost:8000 |
| Postgres | localhost:5432 |

```bash
docker compose up -d --build
```

Keycloak admin: `admin` / `admin` (consola). Usuarios de la app: pass `demo1234`.
Detalle: [Seguridad](seguridad.md).
