export interface Paciente {
  id: number;
  cedula: string;
  nombreCompleto: string;
  telefono: string;
}

/** Datos que el paciente escribe para registrarse en la web (RF2) o walk-in. */
export interface RegistrarPacienteCommand {
  cedula: string;
  nombreCompleto: string;
  telefono: string;
}
