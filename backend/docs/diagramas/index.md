# Diagramas de clases

Diagramas **PlantUML** alineados al código implementado. Se renderizan aquí
en MkDocs (contenedor `docs`, puerto 8000).

## Módulos implementados

| Diagrama | Paquete |
|---|---|
| [Núcleo común](01-nucleo.md) | `com.piedraazul.nucleo` |
| [Identidad](02-identidad.md) | `com.piedraazul.identidad` |
| [Personas](03-personas.md) | `com.piedraazul.personas` |
| [Disponibilidad](04-disponibilidad.md) | `com.piedraazul.disponibilidad` |
| [Citas](05-citas.md) | `com.piedraazul.citas` |

Fuentes `.puml`: [`puml/`](puml/index.md).

!!! note "Qué no aparece"
    No hay módulo de notificaciones ni infraestructura transversal aparte:
    la seguridad JWT vive dentro de **Identidad**.
