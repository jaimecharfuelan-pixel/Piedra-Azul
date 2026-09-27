# db/init

Scripts SQL montados en `/docker-entrypoint-initdb.d`. Postgres solo los
ejecuta al crear el volumen por primera vez.

En este proyecto el esquema lo crea JPA y los datos de demo los carga el
backend (`SeedDatosDemo`). Los `.sql` de esta carpeta no insertan la demo.
