# Seguridad (JWT + Spring Security)

- Sesiones **stateless** (`SessionCreationPolicy.STATELESS`).
- Filtro `JwtAuthenticationFilter` valida el header `Authorization: Bearer <token>`.
- CORS abierto a `http://localhost:*` y `http://127.0.0.1:*` (`CorsConfig`), para
  que el frontend en el 4200 pueda llamar al backend en el 8080.

Configuración en `application.properties`:

```properties
app.security.jwt.secret=...
app.security.jwt.expiration-ms=86400000
```

Clases relevantes:

- `JwtService` — emite y valida tokens
- `JwtAuthenticationFilter` — integra el token en el `SecurityContext`
- `SecurityConfig` — cadena de filtros Spring Security

## Estado actual: la API de negocio está abierta

!!! warning "Pendiente de cablear la autorización por rol"
    El módulo **Identidad (02)** todavía no existe, así que **no hay endpoint de
    login** y no hay forma de obtener un JWT. Para que la aplicación sea usable de
    punta a punta, `SecurityConfig` deja en `permitAll()` los módulos de negocio:

    ```
    /api/auth/**            (reservado para Identidad)
    /api/personas/**
    /api/disponibilidad/**
    /api/citas/**
    /h2-console/**
    /actuator/health
    ```

    Esto es deuda técnica consciente y **no debe salir así a producción**.

## Autorización prevista por rol

Los casos de uso no conocen roles a propósito: la restricción es una decisión de
la capa de entrada. Cuando exista Identidad, ésta es la matriz a aplicar en
`SecurityConfig` (o con `@PreAuthorize` habilitando `@EnableMethodSecurity`):

| Operación | ADMINISTRADOR | AGENDADOR | MEDICO | PACIENTE |
|---|---|---|---|---|
| `PUT /api/disponibilidad/configuracion` | ✔ | — | — | — |
| `POST /api/disponibilidad/periodos` | ✔ | ✔ | ✔ (el suyo) | — |
| `GET /api/disponibilidad/slots` | ✔ | ✔ | ✔ | ✔ |
| `GET /api/citas?medicoId=&fecha=` | ✔ | ✔ | ✔ (la suya) | — |
| `POST /api/citas` | ✔ | ✔ (para cualquiera) | — | ✔ (para sí mismo) |
| `PUT /api/citas/{id}/cancelacion` | ✔ | ✔ | — | ✔ (la suya) |
| `PUT /api/citas/{id}/reagendamiento` | ✔ | ✔ | — | ✔ (la suya) |
| `PUT /api/citas/{id}/atencion` | — | — | ✔ | — |
| `POST /api/personas/medicos` | ✔ | — | — | — |

Los casos marcados "el suyo" / "la suya" necesitan además comprobar la propiedad
del recurso contra el `personaId` del token, no sólo el rol.

## Qué falta para cerrarlo

1. Implementar el módulo Identidad (`Usuario`, `AuthController` con
   `POST /api/auth/login`, `GestionarUsuarioService` usando
   `RegistrarPersonaPort`).
2. Que `JwtAuthenticationFilter` cargue el `RolUsuario` del token como authority
   (`ROLE_MEDICO`, `ROLE_AGENDADOR`, …); hoy pone la lista de authorities vacía.
3. Sustituir los `permitAll()` de negocio por `hasRole(...)` según la matriz.
4. En el frontend, añadir el interceptor que adjunte el token y los guards de ruta;
   hoy no hay ninguno.
