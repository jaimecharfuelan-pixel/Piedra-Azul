# PiedraAzul

Agendamiento de citas (médicas / terapia). Ing. Soft. III — Unicauca.

## Carpetas

| Carpeta | Contenido |
|---|---|
| `backend/` | API Spring Boot (monolito modular) |
| `frontend/` | Angular 19 |
| `keycloak/` | realm del RNF1 |
| `prototipos/` | HTML/CSS/JS del primer corte |
| `diagramas-por-modulo/` | PlantUML de implementación |
| `diagramas-sad/` | contexto, secuencias, despliegue |

## Arranque

Se levanta todo con:

```bash
docker compose up --build -d
```

| Servicio | URL |
|---|---|
| frontend | http://localhost:4200 |
| API | http://localhost:8080 |
| Keycloak | http://localhost:8180 (`admin` / `admin`) |
| docs | `docker compose up docs` → http://localhost:8000 |
| Postgres | `localhost:5432` — `piedraazul` / `piedraazul` |

Base limpia:

```bash
docker compose down -v && docker compose up --build -d
```

## Auth (RNF1)

El JWT lo emite Keycloak. El frontend sigue pegándole a `POST /api/auth/login`;
el backend pide el token y arma la respuesta con `rol` y `personaId`.
Quién puede llamar cada API sigue en `SecurityConfig`.

Usuarios demo (pass `demo1234`):
`admin`, `agendador`, `ana.medico`, `carlos.medico`, `juan.paciente`, `maria.paciente`.

## Tests

```bash
cd backend && mvn test
```
