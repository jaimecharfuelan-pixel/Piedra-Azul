# Identidad

Módulo identidad: se encarga de la identidad con Keycloak. Antes lo hacíamos
con JJWT y acá mismo definíamos el dominio de qué API puede usar cada rol;
eso se mantiene (matriz en `SecurityConfig` + `personaId` local), pero gran
parte de la infraestructura de tokens la abstraemos con Keycloak.

Paquete: `com.piedraazul.identidad` (RF2 + RNF1).

| Pieza | Qué hace |
|---|---|
| `Usuario` | cuenta local (rol + `personaId`) |
| `AutenticarUsuarioService` | login → token del IdP → `TokenResponseDTO` |
| `GestionarUsuarioService` | alta local y sync a Keycloak |
| `ProveedorIdentidadPort` | Keycloak en Docker; Nimbus en tests |
| `JwtAuthConverter` | JWT → `UsuarioAutenticado` |
| `SecurityConfig` | quién puede pegarle a cada ruta |
| `SesionActual` | chequeos de “es mío” en controllers |

El seed de demo no vive acá: está en `com.piedraazul.bootstrap.SeedDatosDemo`.

## Públicos

- `POST /api/auth/login`
- `POST /api/auth/registro-paciente`

Matriz y Keycloak: [Seguridad](seguridad.md).  
Diagrama: [Identidad (PlantUML)](diagramas/02-identidad.md).
