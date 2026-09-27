# Identidad y Acceso

Módulo `com.piedraazul.identidad` (RF2). Concentra usuarios, roles, JWT y
Spring Security. No hay un paquete `infraestructura` transversal aparte:
el filtro JWT, `SecurityConfig` y `SesionActual` viven aquí.

## Qué incluye

| Pieza | Rol |
|---|---|
| `Usuario` | Cuenta con un solo `RolUsuario` y `personaId` opcional |
| `AutenticarUsuarioService` | Login → `TokenResponseDTO` |
| `GestionarUsuarioService` | Alta de usuarios (admin) y alta de paciente en registro público |
| `JwtServiceAdapter` | Emite / valida tokens JJWT |
| `JwtAuthenticationFilter` | `Authorization: Bearer …` → `SecurityContext` |
| `SecurityConfig` | Matriz HTTP por rol |
| `SesionActual` | Propiedad de recurso (✔*) en controllers |
| *(seed demo)* | Ya no vive aquí → `com.piedraazul.bootstrap.SeedDatosDemo` |

## Flujos públicos

- `POST /api/auth/login`
- `POST /api/auth/registro-paciente` (crea `Usuario` PACIENTE + ficha Paciente)

Detalle de endpoints, seed y matriz: [Seguridad (JWT)](seguridad.md).

## Diagrama

Ver [Identidad (PlantUML)](diagramas/02-identidad.md).
