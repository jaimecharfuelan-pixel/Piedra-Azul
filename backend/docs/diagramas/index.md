# Diagramas

PlantUML. MkDocs los pinta con el plugin (`plantuml-markdown` → SVG).

## Clases (por módulo)

| Diagrama | Paquete |
|---|---|
| [Núcleo](01-nucleo.md) | `nucleo` |
| [Identidad](02-identidad.md) | `identidad` |
| [Personas](03-personas.md) | `personas` |
| [Disponibilidad](04-disponibilidad.md) | `disponibilidad` |
| [Citas](05-citas.md) | `citas` |

Fuentes `.puml`: [`puml/`](puml/index.md) y `diagramas-por-modulo/` en la raíz.

## Otros (también PlantUML)

Están embebidos en las páginas (arquitectura, módulos, seguridad, citas, frontend):

- paquetes del monolito
- dependencias entre módulos
- ciclo de vida de Cita
- capas del frontend
- secuencia de login con Keycloak

La seguridad no es un módulo aparte: va dentro de Identidad.
