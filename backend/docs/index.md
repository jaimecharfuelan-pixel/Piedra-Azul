# PiedraAzul

Documentación del monolito modular **PiedraAzul** (Spring Boot + Angular)
servida con **MkDocs Material**.

## Stack

| Tecnología | Uso |
|---|---|
| Spring Boot 3.4 | API REST / monolito modular |
| Spring Security + JJWT | Autenticación Bearer sin estado |
| Spring Data JPA + PostgreSQL | Persistencia |
| Angular 19 | SPA hexagonal (Atomic Design) |
| MkDocs + Material | Este sitio de documentación |
| PlantUML | Diagramas de clases (implementación) |

## Cómo ver la documentación

### Con Docker (recomendado)

Desde la raíz del repo:

```bash
docker compose up docs
```

Abre **http://localhost:8000**

Los diagramas de clases se ven en
[Diagramas](diagramas/index.md) sin abrir otra herramienta.

### En local (sin Docker)

```bash
cd backend
pip install -r requirements-docs.txt
mkdocs serve -a 127.0.0.1:8000
```

## Módulos del monolito

| Módulo | Paquete | Estado |
|---|---|---|
| Núcleo | `nucleo` | Implementado |
| Identidad | `identidad` | Implementado |
| Personas | `personas` | Implementado |
| Disponibilidad | `disponibilidad` | Implementado |
| Citas | `citas` | Implementado |

Ver [Arquitectura](arquitectura.md), [Módulos](modulos.md) y los
[diagramas de clases](diagramas/index.md).

## Aplicación en marcha

| Servicio | URL |
|---|---|
| Frontend | http://localhost:4200 |
| Backend API | http://localhost:8080 |
| Docs (este sitio) | http://localhost:8000 |
| PostgreSQL | localhost:5432 |

```bash
docker compose up -d --build
```
