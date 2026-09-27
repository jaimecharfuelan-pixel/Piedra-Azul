import { EstadoCita } from './estado-cita.enum';

/**
 * Cita tal como la expone el backend (módulo 05).
 * Las horas llegan como 'HH:mm' y la fecha como 'yyyy-MM-dd'.
 */
export interface Cita {
  readonly id: number;
  readonly medicoId: number;
  readonly medicoNombre: string;
  readonly pacienteId: number;
  readonly pacienteNombre: string;
  readonly pacienteTelefono: string;
  readonly fecha: string;
  readonly horaInicio: string;
  readonly horaFin: string;
  readonly duracionMinutos: number;
  readonly estado: EstadoCita;
}

/** Resultado del RF1: listado con la cantidad y el desglose por estado. */
export interface ListadoCitas {
  readonly medicoId: number;
  readonly medicoNombre: string;
  readonly fecha: string;
  readonly orden: string;
  readonly cantidad: number;
  readonly cantidadProgramadas: number;
  readonly cantidadCanceladas: number;
  readonly cantidadAtendidas: number;
  readonly citas: readonly Cita[];
}

export interface AgendarCitaCommand {
  readonly pacienteId: number;
  readonly medicoId: number;
  readonly fecha: string;
  readonly horaInicio: string;
  readonly horaFin: string;
}

export interface ReagendarCitaCommand {
  readonly fecha: string;
  readonly horaInicio: string;
  readonly horaFin: string;
}
