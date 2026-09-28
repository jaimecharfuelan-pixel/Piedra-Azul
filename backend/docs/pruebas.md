# Pruebas

## Qué está cubierto

| Tipo | Dónde | Qué comprueba |
|---|---|---|
| Unitarias de dominio | `src/test/java/.../dominio/` | Entidades y reglas de negocio, sin Spring ni base de datos |
| Unitarias de servicio | `src/test/java/.../aplicacion/servicio/` | Casos de uso y fachadas con puertos simulados (Mockito) |
| Integración por HTTP | `src/test/java/com/piedraazul/citas/FlujoAgendamientoTest.java` | El flujo RF3 → RF2 → RF1 completo con MockMvc y H2 |
| Humo contra el sistema real | `scripts/smoke-test.ps1` | Los tres RF y todos los códigos de error contra el backend en Docker |

```bash
# Unitarias + integración (H2 en memoria, sin Docker)
cd backend
mvn test
```

Resultado esperado: la suite termina con **0 fallos**.

### Detalle de las unitarias

| Clase | Casos |
|---|---|
| `TimeRangeTest` | 3 — duración mínima y solapamiento |
| `ConfiguracionSistemaTest` | 4 — rango de la ventana y `permiteAgendarEn` |
| `PeriodoDisponibilidadTest` | 12 — duración, días, cierre del periodo, rejilla, cruce de fechas |
| `CalculadorSlotsPorIntervaloFijoTest` | 7 — generación de franjas, descanso, ocupadas, no desbordar medianoche |
| `CitaTest` | 7 — ciclo de vida y estados finales |
| `ConsultaTest` | 4 — sólo nace de una cita atendida |
| `MedicoTest`, `PacienteTest`, `EspecialidadTest`, `UsuarioTest` | Alta, validación y estado |
| `DiaSemanaTest`, `OrdenCitasTest`, `SlotDisponibleTest`, `ExcepcionesDeDominioTest` | Traducción de días, orden del listado y códigos de error |
| `*ServiceTest` y `*FacadeTest` | Cada caso de uso con repositorios simulados: éxito y rechazo de la regla |

### Detalle de la integración

`FlujoAgendamientoTest` arranca el contexto completo con `app.seed.enabled=false`,
crea su propio médico y paciente, y recorre:

1. Configura la ventana (RF3) y un horario de 08:00–12:00 en pasos de 30 min.
2. Comprueba que se ofrecen **8 franjas**.
3. Agenda las 08:00 y verifica que quedan **7**.
4. Comprueba que repetir la franja da `SLOT_NO_DISPONIBLE`, y que 08:15 también
   (no coincide con la rejilla).
5. Lista con `HORA_ASC` y `HORA_DESC` verificando `cantidad` y el primer elemento.
6. Cancela, marca atendida, consulta el historial y verifica que una cita atendida
   ya no se puede cancelar.

Además cubre el descanso entre citas, el cierre automático del horario anterior, el
rechazo por ventana y el detalle campo a campo de la validación.

## Prueba de humo contra Docker

Con los contenedores arriba:

```powershell
docker compose up --build -d
powershell -ExecutionPolicy Bypass -File scripts/smoke-test.ps1
```

Resultado esperado: **TODO CORRECTO** (el script registra sus propios pacientes
y médico en cada ejecución).

El script crea su propia especialidad y su propio médico en cada ejecución, así que
se puede repetir sin limpiar la base ni tocar los datos sembrados.

!!! tip "Duración mínima"
    Una cita de 20 minutos se rechaza con `DATOS_INVALIDOS` (Bean Validation en el
    DTO). Para ver `DURACION_CITA_INVALIDA` del dominio hay que enviar una duración
    válida en el formulario pero que no quepa en la franja (p. ej. 120 min en una
    hora). El script de humo cubre ambos casos.

## Pruebas manuales (UI)

Con el stack en marcha (`docker compose up -d`):

1. Iniciar sesión con un usuario demo (`demo1234`).
2. **Configuración** — ventana de semanas y horario de un médico.
3. **Agendar** — elegir médico, fecha y franja; confirmar.
4. **Agenda** — listar, filtrar, cancelar / reagendar / atender (según rol).
5. **Calendario** e **Historial** — estados y consultas creadas al atender.

Detalle de roles y endpoints: [Seguridad](seguridad.md) y [Frontend](frontend.md).

## Qué falta

- Cobertura automatizada del frontend (solo existe el esqueleto de Karma).
- Prueba de concurrencia sobre solapamiento de citas (hoy se resuelve en
  aplicación, sin restricción única en BD).
