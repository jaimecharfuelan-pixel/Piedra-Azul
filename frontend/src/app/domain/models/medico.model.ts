import { Especialidad } from './especialidad.model';

export interface Medico {
  id: number;
  nombreCompleto: string;
  especialidad: Especialidad | null;
  activo: boolean;
}

export interface CrearMedicoCommand {
  nombreCompleto: string;
  especialidadId: number;
}

export interface ActualizarMedicoCommand {
  nombreCompleto: string;
  especialidadId: number;
}
