/**
 * Entrada del historial: lo que queda después de atender una cita.
 */
export interface Consulta {
  readonly id: number;
  readonly citaId: number;
  readonly medicoId: number;
  readonly medicoNombre: string;
  readonly pacienteId: number;
  readonly pacienteNombre: string;
  readonly fecha: string;
  readonly observaciones: string;
  readonly fechaRegistro: string;
}
