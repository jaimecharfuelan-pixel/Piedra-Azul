# Referencia mkdocstrings

## Stack

::: docs_python
    options:
      members:
        - stack_backend

## Núcleo común

::: docs_python.nucleo
    options:
      members:
        - DURACION_MINIMA_CITA_MINUTOS
        - DIAS_SEMANA
        - ROLES_USUARIO
        - ESTADOS_CITA
        - TimeRangeDoc
        - elementos_nucleo

## Personas

::: docs_python.personas
    options:
      members:
        - TABLAS_PERSONAS
        - PUERTOS_PUBLICOS
        - ENDPOINTS
        - resumen_personas

## Disponibilidad

::: docs_python.disponibilidad
    options:
      members:
        - TABLAS_DISPONIBILIDAD
        - VENTANA_MINIMA_SEMANAS
        - VENTANA_MAXIMA_SEMANAS
        - DURACION_CITA_MINIMA_MINUTOS
        - DESCANSO_MAXIMO_MINUTOS
        - PUERTOS_PUBLICOS
        - PUERTOS_CONSUMIDOS
        - ENDPOINTS
        - CODIGOS_ERROR
        - PeriodoDisponibilidadDoc
        - citas_por_dia
        - resumen_disponibilidad

## Citas

::: docs_python.citas
    options:
      members:
        - TABLAS_CITAS
        - ESTADOS_CITA
        - TRANSICIONES
        - ORDENES_LISTADO
        - VALIDACIONES_AGENDAR
        - INDICES
        - PUERTOS_PUBLICOS
        - PUERTOS_CONSUMIDOS
        - ENDPOINTS
        - CODIGOS_ERROR
        - puede_modificarse
        - resumen_citas
