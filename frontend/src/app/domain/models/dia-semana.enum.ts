/** Días de la semana (RF3 — disponibilidad por médico/terapeuta). */
export enum DiaSemana {
  LUNES = 'LUNES',
  MARTES = 'MARTES',
  MIERCOLES = 'MIERCOLES',
  JUEVES = 'JUEVES',
  VIERNES = 'VIERNES',
  SABADO = 'SABADO',
  DOMINGO = 'DOMINGO',
}

/** Orden de lunes a domingo, el mismo que usa el backend. */
export const DIAS_SEMANA: readonly DiaSemana[] = [
  DiaSemana.LUNES,
  DiaSemana.MARTES,
  DiaSemana.MIERCOLES,
  DiaSemana.JUEVES,
  DiaSemana.VIERNES,
  DiaSemana.SABADO,
  DiaSemana.DOMINGO,
];

const ETIQUETAS: Record<DiaSemana, string> = {
  [DiaSemana.LUNES]: 'Lunes',
  [DiaSemana.MARTES]: 'Martes',
  [DiaSemana.MIERCOLES]: 'Miércoles',
  [DiaSemana.JUEVES]: 'Jueves',
  [DiaSemana.VIERNES]: 'Viernes',
  [DiaSemana.SABADO]: 'Sábado',
  [DiaSemana.DOMINGO]: 'Domingo',
};

const ABREVIATURAS: Record<DiaSemana, string> = {
  [DiaSemana.LUNES]: 'Lun',
  [DiaSemana.MARTES]: 'Mar',
  [DiaSemana.MIERCOLES]: 'Mié',
  [DiaSemana.JUEVES]: 'Jue',
  [DiaSemana.VIERNES]: 'Vie',
  [DiaSemana.SABADO]: 'Sáb',
  [DiaSemana.DOMINGO]: 'Dom',
};

export function etiquetaDia(dia: DiaSemana): string {
  return ETIQUETAS[dia];
}

export function abreviaturaDia(dia: DiaSemana): string {
  return ABREVIATURAS[dia];
}

/** Traduce una fecha 'yyyy-MM-dd' al día de la semana del dominio. */
export function diaSemanaDeFecha(fechaIso: string): DiaSemana {
  const [anio, mes, dia] = fechaIso.split('-').map(Number);
  const indice = new Date(anio, mes - 1, dia).getDay();
  // getDay() devuelve 0 para domingo; el dominio empieza en lunes.
  return DIAS_SEMANA[(indice + 6) % 7];
}
