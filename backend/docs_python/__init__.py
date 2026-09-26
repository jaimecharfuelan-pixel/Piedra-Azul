"""
Utilidades de documentación PiedraAzul.

Este módulo existe para que **mkdocstrings** genere referencia automática
desde docstrings (requisito del curso). La lógica de negocio vive en Spring Boot.
"""


def stack_backend() -> dict[str, str]:
    """Devuelve el stack declarado del backend.

    Returns:
        Diccionario tecnología → rol en el proyecto.
    """
    return {
        "Spring Boot": "API REST / monolito modular",
        "Spring Security": "Autenticación y autorización",
        "JWT (jjwt)": "Tokens Bearer",
        "MkDocs + mkdocstrings": "Documentación",
    }
