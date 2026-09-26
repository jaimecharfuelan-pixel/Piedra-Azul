export type CitaEstado = 'PROGRAMADA' | 'CONFIRMADA' | 'CANCELADA' | 'COMPLETADA';

export interface Cita {
  id: string;
  medicoId: string;
  pacienteId: string;
  inicio: string;
  fin: string;
  estado: CitaEstado;
  titulo?: string;
}
