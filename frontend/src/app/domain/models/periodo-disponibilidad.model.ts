import { DiaSemana } from './dia-semana.enum';

/**
 * Horario de atención de un médico vigente en un rango de fechas (RF3).
 * `fechaFin === null` significa que sigue rigiendo.
 */
export interface PeriodoDisponibilidad {
  readonly id: number;
  readonly medicoId: number;
  readonly medicoNombre: string;
  readonly fechaInicio: string;
  readonly fechaFin: string | null;
  readonly diasAtencion: readonly DiaSemana[];
  readonly horaInicio: string;
  readonly horaFin: string;
  readonly duracionCitaMinutos: number;
  readonly descansoEntreCitasMinutos: number;
  readonly vigente: boolean;
}

export interface ConfigurarPeriodoCommand {
  readonly medicoId: number;
  readonly fechaInicio: string;
  readonly fechaFin: string | null;
  readonly diasAtencion: readonly DiaSemana[];
  readonly horaInicio: string;
  readonly horaFin: string;
  readonly duracionCitaMinutos: number;
  readonly descansoEntreCitasMinutos: number;
}

export const DURACION_CITA_MINIMA_MINUTOS = 30;
export const DESCANSO_MAXIMO_MINUTOS = 120;
