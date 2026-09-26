# Arquitectura

Monolito modular con **Dependency Inversion** entre módulos: la comunicación
pasa por puertos (interfaces) declarados por el consumidor e implementados
por fachadas del módulo proveedor.

```
com.piedraazul
├── sharedkernel      # Value objects, IDs, enums compartidos
├── identidad         # RF2 — usuarios, roles, autenticación
├── personas          # RF1/RF2 — pacientes, médicos, especialidades
├── disponibilidad    # RF3 — ventanas, festivos, franjas
├── citas             # RF1/RF2 — agenda y ciclo de vida de citas
├── notificaciones    # in-app y email
└── infraestructura   # Security/JWT, errores, eventos en proceso
```

Los eventos de dominio se publican **en proceso** (sin RabbitMQ) mediante
`ApplicationEventPublisher` de Spring.
