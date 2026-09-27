export interface Paciente {
  id: number;
  nombreCompleto: string;
  telefono: string;
}

/** Datos que el paciente escribe para registrarse en la web (RF2). */
export interface RegistrarPacienteCommand {
  nombreCompleto: string;
  telefono: string;
}
