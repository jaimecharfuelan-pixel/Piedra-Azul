export interface Especialidad {
  id: number;
  nombre: string;
  activa: boolean;
}

export interface CrearEspecialidadCommand {
  nombre: string;
}
