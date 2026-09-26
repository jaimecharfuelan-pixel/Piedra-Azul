# Seguridad (JWT + Spring Security)

- Sesiones **stateless** (`SessionCreationPolicy.STATELESS`).
- Filtro `JwtAuthenticationFilter` valida el header `Authorization: Bearer <token>`.
- Endpoints públicos iniciales: `/api/auth/**`, consola H2, health.
- El resto de la API exige autenticación.

Configuración en `application.properties`:

```properties
app.security.jwt.secret=...
app.security.jwt.expiration-ms=86400000
```

Clases relevantes:

- `JwtService` — emite y valida tokens
- `JwtAuthenticationFilter` — integra el token en el `SecurityContext`
- `SecurityConfig` — cadena de filtros Spring Security
