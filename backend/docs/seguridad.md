# Seguridad (JWT + Spring Security)

- Sesiones **stateless** (`SessionCreationPolicy.STATELESS`).
- Filtro `JwtAuthenticationFilter` valida `Authorization: Bearer <token>` y
  carga `UsuarioAutenticado` (rol + `personaId`) en el `SecurityContext`.
- Roles en `SecurityConfig` (módulo Identidad). Propiedad de recurso (✔*) en
  controllers vía `SesionActual` — los `*Service` de negocio no reciben el rol.
- CORS: `http://localhost:*` y `http://127.0.0.1:*`.

```properties
app.security.jwt.secret=...
app.security.jwt.expiration-ms=86400000
```

## Endpoints públicos

| Método | Ruta |
|---|---|
| `POST` | `/api/auth/login` |
| `POST` | `/api/auth/registro-paciente` |
| `GET` | `/actuator/health` |

## Usuarios demo (seed)

Password de todos: `demo1234`

| Username | Rol |
|---|---|
| `admin` | ADMINISTRADOR |
| `agendador` | AGENDADOR |
| `ana.medico` / `carlos.medico` | MEDICO |
| `juan.paciente` / `maria.paciente` | PACIENTE |

## Matriz de roles (resumen)

| Área | ADMIN | AGENDADOR | MEDICO | PACIENTE |
|---|:---:|:---:|:---:|:---:|
| CRUD usuarios | ✔ | — | — | — |
| Especialidades / médicos (escritura) | ✔ | — | — | — |
| Registrar paciente walk-in | ✔ | ✔ | ✔ | — |
| Ventana agendamiento | ✔ | — | — | — |
| Periodos horario | ✔ | ✔ | ✔\* | — |
| Slots | ✔ | ✔ | ✔ | ✔ |
| Agendar / cancelar / reagendar | ✔ | ✔ | ✔ | ✔\* |
| Marcar atendida | — | — | ✔\* | — |
| Agenda del día | ✔\*UI | ✔ | ✔\* | — |

En la **UI**, el administrador no ve Agenda del día (configura el centro;
cancelar/atender lo hacen agendador y médico). El backend aún autoriza
`GET /api/citas` a ADMIN si se llama por API.

\* = solo su `personaId` (`SesionActual`).  
\*UI = permitido en API; oculto en navegación Angular.
