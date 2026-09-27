import { RolUsuario } from '../models/rol-usuario.enum';

export type AccionUi =
  | 'panel'
  | 'agenda'
  | 'agendar'
  | 'calendario'
  | 'configuracion'
  | 'historial'
  | 'personas';

/**
 * Navegación / paneles por rol (alineado a SecurityConfig del backend).
 */
const PERMISOS: Readonly<Record<RolUsuario, readonly AccionUi[]>> = {
  [RolUsuario.ADMINISTRADOR]: [
    'panel',
    'agendar',
    'calendario',
    'configuracion',
    'historial',
    'personas',
  ],
  [RolUsuario.AGENDADOR]: [
    'panel',
    'agenda',
    'agendar',
    'calendario',
    'configuracion',
    'historial',
    'personas',
  ],
  [RolUsuario.MEDICO]: [
    'panel',
    'agenda',
    'agendar',
    'calendario',
    'configuracion',
    'historial',
    'personas',
  ],
  [RolUsuario.PACIENTE]: ['panel', 'agendar', 'historial'],
};

export function accionesParaRol(rol: RolUsuario | null | undefined): readonly AccionUi[] {
  if (!rol) {
    return [];
  }
  return PERMISOS[rol] ?? [];
}

export function rolPuede(rol: RolUsuario | null | undefined, accion: AccionUi): boolean {
  return accionesParaRol(rol).includes(accion);
}

export function rutaPanelPorRol(rol: RolUsuario): string {
  switch (rol) {
    case RolUsuario.ADMINISTRADOR:
      return '/panel/admin';
    case RolUsuario.AGENDADOR:
      return '/panel/agendador';
    case RolUsuario.MEDICO:
      return '/panel/medico';
    case RolUsuario.PACIENTE:
      return '/panel/paciente';
    default:
      return '/panel';
  }
}

/** Solo ADMIN crea especialidades y médicos. */
export function puedeCrearCatalogoPersonas(rol: RolUsuario | null | undefined): boolean {
  return rol === RolUsuario.ADMINISTRADOR;
}

/** Admin, agendador y médico registran pacientes walk-in. */
export function puedeRegistrarPacienteWalkIn(rol: RolUsuario | null | undefined): boolean {
  return (
    rol === RolUsuario.ADMINISTRADOR ||
    rol === RolUsuario.AGENDADOR ||
    rol === RolUsuario.MEDICO
  );
}

/** Solo ADMIN cambia la ventana global de agendamiento. */
export function puedeEditarVentana(rol: RolUsuario | null | undefined): boolean {
  return rol === RolUsuario.ADMINISTRADOR;
}

/** El médico solo opera sobre su propio personaId. */
export function debeFijarMedicoPropio(rol: RolUsuario | null | undefined): boolean {
  return rol === RolUsuario.MEDICO;
}

/** Solo el médico marca citas como atendidas. */
export function puedeAtenderCita(rol: RolUsuario | null | undefined): boolean {
  return rol === RolUsuario.MEDICO;
}

/** Admin, agendador y médico pueden cancelar/reagendar. */
export function puedeCancelarReagendar(rol: RolUsuario | null | undefined): boolean {
  return rol === RolUsuario.AGENDADOR || rol === RolUsuario.MEDICO;
}

/** Paciente solo ve su propio historial (sin listas ni pestañas). */
export function historialSoloPropio(rol: RolUsuario | null | undefined): boolean {
  return rol === RolUsuario.PACIENTE;
}

/** Listado de pacientes: admin, agendador y médico (no paciente). */
export function puedeListarPacientes(rol: RolUsuario | null | undefined): boolean {
  return (
    rol === RolUsuario.ADMINISTRADOR ||
    rol === RolUsuario.AGENDADOR ||
    rol === RolUsuario.MEDICO
  );
}

/** Historial por médico: admin, agendador y médico (paciente no). */
export function puedeHistorialPorMedico(rol: RolUsuario | null | undefined): boolean {
  return (
    rol === RolUsuario.ADMINISTRADOR ||
    rol === RolUsuario.AGENDADOR ||
    rol === RolUsuario.MEDICO
  );
}
