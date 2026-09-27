/**
 * Franja libre que el paciente puede elegir (RF2).
 */
export interface SlotDisponible {
  readonly fecha: string;
  readonly horaInicio: string;
  readonly horaFin: string;
}

/** Identificador estable de una franja, útil como clave de lista y de selección. */
export function claveSlot(slot: SlotDisponible): string {
  return `${slot.fecha}T${slot.horaInicio}`;
}
