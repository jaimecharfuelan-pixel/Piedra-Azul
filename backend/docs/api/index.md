# API

Base: `http://localhost:8080`. Todas las respuestas son JSON.

## Convenciones

| Tipo | Formato | Ejemplo |
|---|---|---|
| Fecha | `yyyy-MM-dd` | `2026-09-29` |
| Hora | `HH:mm` (a la entrada también se acepta `HH:mm:ss`) | `08:30` |
| Fecha y hora | ISO-8601 local | `2026-09-29T10:15:30` |

## Errores

Todos los errores siguen RFC 7807 (`ProblemDetail`) con dos campos extra:

```json
{
  "type": "about:blank",
  "title": "SLOT_NO_DISPONIBLE",
  "status": 409,
  "detail": "Ese horario acaba de ser tomado por otro paciente; elige otra franja",
  "codigo": "SLOT_NO_DISPONIBLE",
  "timestamp": "2026-09-26T23:40:11.204Z",
  "extra": {}
}
```

Cuando falla la validación del formulario, `codigo` es `DATOS_INVALIDOS` y `extra`
trae el detalle campo a campo:

```json
{
  "status": 400,
  "codigo": "DATOS_INVALIDOS",
  "detail": "Hay campos inválidos en la solicitud",
  "extra": { "medicoId": "Selecciona un médico" }
}
```

Mapeo de estado: `RecursoNoEncontradoException` → 404;
`ReglaDeNegocioException` → 409 si el código representa un choque con el estado
actual del sistema, 400 si es una petición mal formada. La lista concreta está en
`NucleoExceptionHandler`.

## Personas — `/api/personas`

Ver [Personas](../personas.md).

| Método | Ruta |
|---|---|
| POST / GET | `/especialidades` |
| PUT | `/especialidades/{id}/estado` |
| POST / GET | `/medicos` |
| PUT | `/medicos/{id}`, `/medicos/{id}/estado` |
| GET | `/medicos/{id}` |
| POST | `/pacientes` | RF2 registro |
| GET | `/pacientes`, `/pacientes/{id}` |

## Disponibilidad — `/api/disponibilidad`

Ver [Disponibilidad](../disponibilidad.md).

| Método | Ruta | RF |
|---|---|---|
| GET | `/configuracion` | RF3 |
| PUT | `/configuracion` | RF3 |
| POST | `/periodos` | RF3 |
| GET | `/periodos?medicoId=` | RF3 |
| GET | `/slots?medicoId=&fecha=` | RF2 |
| GET | `/slots/rango?medicoId=&desde=&hasta=` | RF2 |

## Citas — `/api/citas`

Ver [Citas](../citas.md).

| Método | Ruta | RF |
|---|---|---|
| POST | `/` | RF2 |
| PUT | `/{citaId}/cancelacion` | RF2 |
| PUT | `/{citaId}/reagendamiento` | RF2 |
| PUT | `/{citaId}/atencion` | — |
| GET | `/{citaId}` | — |
| GET | `/?medicoId=&fecha=&orden=` | **RF1** |
| GET | `/rango?medicoId=&desde=&hasta=` | RF1 |
| GET | `/paciente/{pacienteId}` | RF2 |
| GET | `/historial/paciente/{pacienteId}` | — |
| GET | `/historial/medico/{medicoId}` | — |

## Auth — `/api/auth`

Pendiente (módulo Identidad). Ver [Seguridad](../seguridad.md).
