# PiedraAzul — Frontend

Angular 19 (standalone), arquitectura hexagonal y FullCalendar.

```
src/app/
├── domain/            # modelos y puertos
├── application/       # casos de uso
├── infrastructure/    # HTTP, auth, calendario
└── presentation/      # páginas y componentes
```

Los puertos se enlazan en `app.config.ts`.

## Ejecutar

```bash
npm install
npm start
```

http://localhost:4200 — el API debe estar en http://localhost:8080.

| Ruta | Uso |
|---|---|
| `/login`, `/registro` | Autenticación JWT |
| `/agenda` | Citas del día |
| `/agendar` | Reserva de franjas |
| `/calendario` | Vista calendario |
| `/configuracion` | Ventana y horarios |
| `/historial` | Consultas atendidas |
| `/personas` | Catálogo |
