# PiedraAzul Backend

Documentación del monolito modular **PiedraAzul** construido con Spring Boot.

## Stack

| Tecnología | Uso |
|---|---|
| Spring Boot 3.4 | API REST / monolito modular |
| Spring Security | Autorización y autenticación |
| JWT (jjwt) | Tokens Bearer sin estado |
| Spring Data JPA + H2 | Persistencia (dev) |
| MkDocs + Material + mkdocstrings | Sitio de documentación |

## Cómo generar la documentación

```bash
pip install -r requirements-docs.txt
mkdocs serve
```

Abre http://127.0.0.1:8000

```bash
mkdocs build
```

## Módulos del monolito

Ver [Arquitectura](arquitectura.md) y los diagramas Mermaid en `diagramas/`.
