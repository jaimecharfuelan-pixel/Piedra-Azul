# Diagrama de Clases — Monolito Modular Piedrazul 2026.2

El archivo maestro `../DiagramaDeClasesMonolito2026-2.mmd` contiene el diseño completo
(275 clases, 406 relaciones) y es la **fuente de verdad** para implementar cada módulo.

Ese archivo maestro mide ~146 KB y **no se puede renderizar directamente en Draw.io**:
Mermaid aborta con `Maximum text size in diagram exceeded` porque su límite por defecto
es de 50.000 caracteres, y ese límite es una clave protegida del motor que no se puede
elevar con una directiva `%%{init: {"maxTextSize": ...}}%%`.

Por eso esta carpeta contiene el mismo diagrama dividido por módulo. Cada archivo está
por debajo del límite y fue verificado renderizando de verdad con Mermaid 10.9 y 11.17.

## Cómo insertarlos en Draw.io

1. Abre Draw.io.
2. `Extras > Editar diagrama...`, o bien `Insertar (+) > Avanzado > Mermaid`.
3. Pega el contenido completo de uno de los archivos `.mmd`.
4. Repite en pestañas distintas para cada módulo.

## Archivos

| Archivo | Contenido | Requisito | Clases | Relaciones |
|---|---|---|---|---|
| `00-VistaGeneralModulos.mmd` | Módulos y puertos entre módulos (nivel C4 Componentes) | — | 15 | 26 |
| `01-SharedKernel.mmd` | Value Objects, IDs, enums y contratos compartidos | — | 17 | 4 |
| `02-ModuloIdentidadAcceso.mmd` | Usuario, roles, autenticación, proveedor de identidad | RF2 | 36 | 41 |
| `03-ModuloGestionPersonas.mmd` | Persona, Paciente, Médico, especialidades | RF1, RF2 | 73 | 89 |
| `04-ModuloConfiguracionDisponibilidad.mmd` | Ventana de semanas, festivos, días, franjas e intervalos | **RF3** | 60 | 80 |
| `05a-ModuloCitas-Dominio.mmd` | Agregado `Cita`, patrón State, Builders, Policies | RF1, RF2 | 48 | 63 |
| `05b-ModuloCitas-AplicacionInfra.mmd` | Controladores, casos de uso, puertos, persistencia | **RF1, RF2** | 70 | 104 |
| `06-ModuloNotificaciones.mmd` | Notificaciones in-app y email (Strategy) | — | 33 | 37 |
| `07-InfraestructuraTransversal.mmd` | Seguridad, manejo de errores, eventos en proceso | — | 23 | 21 |

Las clases que un módulo necesita de otro (por ejemplo `MedicoReadModel` o `AgendaMedico`
dentro del módulo de Citas) aparecen repetidas en los archivos que las consumen, dentro de
su `namespace` original, para que los contratos entre módulos queden visibles en cada vista.

## Trazabilidad con el código del semestre anterior

Cada clase adaptada del proyecto de microservicios lo indica de dos formas:

- En el estereotipo visible, por ejemplo `<<Service adaptado de CitaService>>`.
- En un comentario `%%` inmediatamente encima de la declaración de la clase, que también
  documenta las interfaces y los enums (donde el estereotipo debe ser `<<interface>>`
  o `<<enumeration>>` para que Mermaid los dibuje correctamente).

## Decisiones de migración de microservicios a monolito modular

- Se eliminan `MedicoSnapshot`, `PacienteSnapshot` y `DisponibilidadSnapshot` junto con sus
  consumidores de RabbitMQ. Se reemplazan por puertos de módulo (`CatalogoPersonasPort`,
  `DisponibilidadConsultaPort`, `ConfiguracionConsultaPort`) y read models de solo lectura,
  con consistencia inmediata sobre una única base de datos.
- Se elimina la llamada HTTP `CitasServiceClient` de personas hacia citas. La dependencia se
  invierte con `VerificadorCitasActivasPort`, declarado en el módulo de Configuración e
  implementado por `CitasActivasAdapter` en el módulo de Citas.
- Se elimina RabbitMQ. Los eventos de dominio se publican en proceso mediante
  `DomainEventPublisher` sobre `ApplicationEventPublisher` de Spring.
- Se unifica el `String diaSemana` de `personas-service` y el `DayOfWeek` de `citas-service`
  en un único enum `DiaSemana` del Shared Kernel, que reemplaza al enum vacío
  `DiaSemanaLaboral` y a la clase utilitaria `DiaSemanaMapper`.
- El bucle de generación de horarios que estaba embebido en
  `ConsultarSlotsDisponiblesService` se extrae al servicio de dominio `CalculadorDeSlots`.
