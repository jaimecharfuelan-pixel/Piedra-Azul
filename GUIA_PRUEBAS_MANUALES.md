# Guía de pruebas manuales

Recorre los tres requisitos funcionales desde el navegador. El stack Docker
deja datos de demostración listos (Dra. Ana Pérez, Ft. Carlos Muñoz, Juan
Ramírez y María Gómez) la primera vez que arranca contra una base vacía.

## Arrancar

```bash
docker compose up --build -d
```

| Qué | URL |
|---|---|
| Frontend | http://localhost:4200 |
| Backend | http://localhost:8080 |
| Salud | http://localhost:8080/actuator/health |

Espera a que `docker compose ps` muestre los tres contenedores en `healthy` /
`running` (el frontend espera al backend).

## Configurar disponibilidad

1. Abre http://localhost:4200/configuracion
2. Cambia **Semanas habilitadas** a `6` y pulsa **Guardar ventana**.
   Debe aparecer el rango *agendamientoDesde* → *agendamientoHasta*.
3. Elige **Dra. Ana Pérez**.
4. Marca lunes a viernes, franja `08:00`–`12:00`, duración `30`, descanso `0`.
5. Pulsa **Guardar horario**. La tarjeta de periodos vigentes se actualiza.
6. Cambia a **Ft. Carlos Muñoz**: debe mostrar 14:00–18:00, citas de 45 min y
   15 min de descanso (datos sembrados).
7. Prueba un error: duración `20` → al pulsar **Guardar horario** el formulario
   pide el mínimo de 30 minutos.
   Duración `120` en una franja de una hora → el backend responde
   `DURACION_CITA_INVALIDA`.
8. Un médico nuevo, sin filas en **Horarios guardados**, también deja pulsar
   **Guardar horario** (lunes a viernes, 08:00–12:00, 30 min y 0 de descanso).

## Agendar por la web

1. Abre http://localhost:4200/agendar
2. Si eres un paciente nuevo, pulsa **Soy un paciente nuevo**, escribe nombre y
   teléfono (7–15 dígitos) y confirma. Quedas seleccionado.
   Si ya estás registrado, elige **Juan Ramírez**.
3. Elige **Dra. Ana Pérez** y un día hábil dentro de la ventana (mañana o el
   siguiente lunes si hoy es domingo).
4. Aparecen las franjas libres (rejilla de 30 min). El calendario de la semana
   muestra los mismos bloques.
5. Elige una franja, pulsa **Agendar cita** y confirma.
6. La franja desaparece de la lista. La cita queda en **Mis próximas citas**.
7. Cancela esa cita: la franja vuelve a ofrecerse.

## Agenda del día

1. Abre http://localhost:4200/agenda
2. Elige el mismo médico y la misma fecha de la cita que acabas de crear.
3. Pulsa **Buscar citas**.
4. Las tarjetas de resumen muestran el **total** y el desglose por estado.
5. La tabla se puede ordenar pulsando **Hora**, **Paciente** o **Estado**.
6. Filtra por nombre de paciente o por estado; el total no cambia, sólo las filas
   visibles.
7. Desde la fila: **Reagendar** (elige otra franja), **Atender** (escribe
   observaciones) o **Cancelar**.

## Calendario, historial y personas

| Ruta | Qué comprobar |
|---|---|
| `/calendario` | Las citas aparecen con color: azul programada, verde atendida, gris cancelada |
| `/historial` | Tras atender una cita, la consulta queda en el historial |
| `/personas` | Puedes crear especialidad, médico y registrar un paciente |

## Datos de demostración

| Quién | Horario | Para qué |
|---|---|---|
| Dra. Ana Pérez | Lun–sáb 08:00–12:00, citas de 30 min | Agenda de mañana |
| Ft. Carlos Muñoz | Todos los días 14:00–18:00, 45 min + 15 de descanso | Agenda de tarde |
| Juan Ramírez / María Gómez | Pacientes registrados | Agendar sin crear uno nuevo |

Si la base ya tenía datos, el seed no vuelve a escribir. Para empezar de cero:

```bash
docker compose down -v
docker compose up --build -d
```
