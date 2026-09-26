import { EstadoCita } from './estado-cita.enum';

export interface Cita {
  id: string;
  medicoId: string;
  pacienteId: string;
  inicio: string;
  fin: string;
  estado: EstadoCita;
  titulo?: string;
}
