# Pruebas

## Qué está cubierto

| Tipo | Dónde | Qué comprueba |
|---|---|---|
| Unitarias de dominio | `src/test/java/.../dominio/` | Reglas de negocio sin Spring ni base de datos |
| Integración por HTTP | `src/test/java/com/piedraazul/citas/FlujoAgendamientoTest.java` | El flujo RF3 → RF2 → RF1 completo con MockMvc y H2 |
| Humo contra el sistema real | `scripts/smoke-test.ps1` | Los tres RF y todos los códigos de error contra el backend en Docker |

```bash
# Unitarias + integración (H2 en memoria, sin Docker)
cd backend
mvn test
```

Resultado esperado: **44 pruebas, 0 fallos**.

### Detalle de las unitarias

| Clase | Casos |
|---|---|
| `TimeRangeTest` | 3 — duración mínima y solapamiento |
| `ConfiguracionSistemaTest` | 4 — rango de la ventana y `permiteAgendarEn` |
| `PeriodoDisponibilidadTest` | 12 — duración, días, cierre del periodo, rejilla, cruce de fechas |
| `CalculadorSlotsPorIntervaloFijoTest` | 7 — generación de franjas, descanso, ocupadas, no desbordar medianoche |
| `CitaTest` | 7 — ciclo de vida y estados finales |
| `ConsultaTest` | 4 — sólo nace de una cita atendida |

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

!!! tip "La duración mínima está protegida dos veces"
    Una cita de 20 minutos se rechaza con `DATOS_INVALIDOS`, porque Bean Validation
    la para en el DTO antes de llegar al dominio. Para ver la regla del dominio
    (`DURACION_CITA_INVALIDA`) hay que enviar una duración que pase el formulario
    pero no quepa en la franja, por ejemplo 120 minutos en una franja de una hora.
    El script comprueba los dos caminos.

## Pruebas manuales por interfaz

Ver la [guía de pruebas manuales](../../GUIA_PRUEBAS_MANUALES.md) en la raíz del
repositorio: recorre las tres pantallas paso a paso indicando qué debe pasar en
cada una.

## Qué falta

- Pruebas del frontend (Karma/Jasmine está configurado pero sólo existe el
  esqueleto `app.component.spec.ts`).
- Pruebas de concurrencia sobre `existeSolapamiento`: hoy dos peticiones
  simultáneas sobre la misma franja se resuelven por la comprobación previa, no por
  una restricción única en la base de datos.
