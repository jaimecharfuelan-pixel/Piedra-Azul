import { Cita } from './cita.model';

/** Respuesta de GET /api/citas/paciente/cedula/{cedula} */
export interface HistorialCitasPorCedula {
  cedula: string;
  pacienteId: number;
  pacienteNombre: string;
  cantidad: number;
  citas: Cita[];
}
