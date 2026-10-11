# Backend

Spring Boot 3.4 / Java 17. Monolito modular.

En Docker el JWT sale de Keycloak. En local y en tests se usa
`app.security.provider=local` (Nimbus), para no depender del contenedor.

## Perfiles

| Perfil | BD | Auth |
|---|---|---|
| `local` | H2 | Nimbus |
| `docker` | Postgres | Keycloak `:8180` |

Login: `POST /api/auth/login`. Matriz de roles: `SecurityConfig`.
Más detalle en `docs/seguridad.md`.

## Seed

Con `APP_SEED_ENABLED=true` corre `SeedDatosDemo` si no existe `admin`.
Pass demo: `demo1234`.

```bash
docker compose down -v
docker compose up -d --build
```

## Comandos

```bash
mvn spring-boot:run
mvn test
```

Docs: `docker compose up docs` → http://localhost:8000
