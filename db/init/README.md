# Base de datos — scripts de inicialización

Coloca archivos `.sql` en esta carpeta (`db/init/`).

Docker Postgres los ejecuta **solo la primera vez** que se crea el volumen
(`postgres_data`), en orden alfabético.

- `01-datos-prueba.sql` — plantilla vacía para INSERTs de prueba
- El esquema de tablas lo genera **JPA/Hibernate** desde el backend
