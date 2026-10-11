# Frontend

Angular 19 standalone. Capas domain / application / infrastructure / presentation.

```
src/app/
├── domain/
├── application/
├── infrastructure/
└── presentation/
```

Los puertos se cablean en `app.config.ts`.

## Arranque

```bash
npm install
npm start
```

Se abre en http://localhost:4200. La API tiene que estar en `:8080`.

El login va a `/api/auth/login`. En Docker el token es de Keycloak; el interceptor
manda el Bearer en las demás llamadas.

| Ruta | Uso |
|---|---|
| `/login`, `/registro` | auth |
| `/agenda` | agenda del día + CSV |
| `/agendar` | reserva |
| `/calendario` | FullCalendar |
| `/configuracion` | ventana / horarios |
| `/historial` | consultas atendidas |
| `/citas-por-cedula` | RF5 (agendador / médico) |
| `/personas` | catálogo |
