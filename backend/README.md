# PiedraAzul — Backend

API REST en Spring Boot 3.4 (Java 17): monolito modular, Spring Security + JWT,
JPA y PostgreSQL (H2 en perfil local).

## Perfiles

| Perfil | Uso | BD |
|---|---|---|
| `local` (default) | `mvn spring-boot:run` | H2 en memoria |
| `docker` | Compose | PostgreSQL |

## Datos de demo

Con `APP_SEED_ENABLED=true`, al arrancar se ejecuta
`com.piedraazul.bootstrap.SeedDatosDemo` (`ApplicationRunner`) si no existe el
usuario `admin`. Crea usuarios, horarios y citas vía casos de uso.

Password de demo: `demo1234`.

```bash
# regenerar datos
docker compose down -v
docker compose up -d --build
```

## Comandos

```bash
mvn spring-boot:run
mvn test
```

Documentación MkDocs: desde la raíz del repo, `docker compose up docs`
(http://localhost:8000).
