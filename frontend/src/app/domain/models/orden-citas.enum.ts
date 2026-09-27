/**
 * Criterios de ordenamiento que acepta el listado del RF1.
 * Deben coincidir con el enum OrdenCitas del backend.
 */
export enum OrdenCitas {
  HORA_ASC = 'HORA_ASC',
  HORA_DESC = 'HORA_DESC',
  ESTADO_ASC = 'ESTADO_ASC',
  ESTADO_DESC = 'ESTADO_DESC',
  PACIENTE_ASC = 'PACIENTE_ASC',
  PACIENTE_DESC = 'PACIENTE_DESC',
}

/** Columnas de la tabla que se pueden ordenar, con su par ascendente/descendente. */
export const COLUMNAS_ORDENABLES = [
  { campo: 'hora', etiqueta: 'Hora', asc: OrdenCitas.HORA_ASC, desc: OrdenCitas.HORA_DESC },
  { campo: 'paciente', etiqueta: 'Paciente', asc: OrdenCitas.PACIENTE_ASC, desc: OrdenCitas.PACIENTE_DESC },
  { campo: 'estado', etiqueta: 'Estado', asc: OrdenCitas.ESTADO_ASC, desc: OrdenCitas.ESTADO_DESC },
] as const;

export type CampoOrdenable = (typeof COLUMNAS_ORDENABLES)[number]['campo'];

export function esDescendente(orden: OrdenCitas): boolean {
  return orden.endsWith('_DESC');
}

/**
 * Alterna el sentido si se vuelve a pulsar la misma columna; si es otra,
 * empieza en ascendente.
 */
export function alternarOrden(actual: OrdenCitas, campo: CampoOrdenable): OrdenCitas {
  const columna = COLUMNAS_ORDENABLES.find((c) => c.campo === campo);
  if (!columna) {
    return actual;
  }
  return actual === columna.asc ? columna.desc : columna.asc;
}
