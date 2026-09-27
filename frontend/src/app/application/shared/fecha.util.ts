/**
 * Utilidades de fecha en formato ISO 'yyyy-MM-dd', el mismo que habla la API.
 *
 * Se construyen las cadenas a mano en lugar de usar toISOString() porque este
 * último convierte a UTC y, en zonas con desplazamiento negativo como Colombia,
 * devuelve el día anterior.
 */

export function aFechaIso(fecha: Date): string {
  const anio = fecha.getFullYear();
  const mes = `${fecha.getMonth() + 1}`.padStart(2, '0');
  const dia = `${fecha.getDate()}`.padStart(2, '0');
  return `${anio}-${mes}-${dia}`;
}

export function hoyIso(): string {
  return aFechaIso(new Date());
}

export function desdeFechaIso(fechaIso: string): Date {
  const [anio, mes, dia] = fechaIso.split('-').map(Number);
  return new Date(anio, mes - 1, dia);
}

export function sumarDiasIso(fechaIso: string, dias: number): string {
  const fecha = desdeFechaIso(fechaIso);
  fecha.setDate(fecha.getDate() + dias);
  return aFechaIso(fecha);
}

/** Lunes de la semana a la que pertenece la fecha. */
export function inicioDeSemanaIso(fechaIso: string): string {
  const fecha = desdeFechaIso(fechaIso);
  const desplazamiento = (fecha.getDay() + 6) % 7;
  fecha.setDate(fecha.getDate() - desplazamiento);
  return aFechaIso(fecha);
}

/** Texto legible para encabezados, ej. "viernes, 3 de abril de 2026". */
export function fechaLegible(fechaIso: string): string {
  return desdeFechaIso(fechaIso).toLocaleDateString('es-CO', {
    weekday: 'long',
    day: 'numeric',
    month: 'long',
    year: 'numeric',
  });
}

/** Combina fecha y hora ISO en el formato que espera FullCalendar. */
export function aFechaHoraIso(fechaIso: string, hora: string): string {
  const horaNormalizada = hora.length === 5 ? `${hora}:00` : hora;
  return `${fechaIso}T${horaNormalizada}`;
}
