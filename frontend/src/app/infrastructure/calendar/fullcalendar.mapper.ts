import { EventInput } from '@fullcalendar/angular';
import { aFechaHoraIso } from '../../application/shared/fecha.util';
import { Cita } from '../../domain/models/cita.model';
import { EstadoCita } from '../../domain/models/estado-cita.enum';
import { claveSlot, SlotDisponible } from '../../domain/models/slot-disponible.model';

interface Paleta {
  readonly backgroundColor: string;
  readonly borderColor: string;
  readonly textColor: string;
}

/** Mismos colores semánticos que los badges de estado. */
const PALETA_POR_ESTADO: Readonly<Record<EstadoCita, Paleta>> = {
  [EstadoCita.PROGRAMADA]: {
    backgroundColor: '#2170c4',
    borderColor: '#14487f',
    textColor: '#ffffff',
  },
  [EstadoCita.ATENDIDA]: {
    backgroundColor: '#10633f',
    borderColor: '#0b4a2f',
    textColor: '#ffffff',
  },
  [EstadoCita.CANCELADA]: {
    backgroundColor: '#eef2f7',
    borderColor: '#c3cdda',
    textColor: '#6b7889',
  },
};

const PALETA_SLOT_LIBRE: Paleta = {
  backgroundColor: '#e3eefb',
  borderColor: '#7fb2e8',
  textColor: '#0a2b52',
};

const PALETA_SLOT_ELEGIDO: Paleta = {
  backgroundColor: '#14487f',
  borderColor: '#0a2b52',
  textColor: '#ffffff',
};

export function paletaDeEstado(estado: string | undefined | null): Paleta {
  if (estado === EstadoCita.ATENDIDA || estado === 'ATENDIDA') {
    return PALETA_POR_ESTADO[EstadoCita.ATENDIDA];
  }
  if (estado === EstadoCita.CANCELADA || estado === 'CANCELADA') {
    return PALETA_POR_ESTADO[EstadoCita.CANCELADA];
  }
  return PALETA_POR_ESTADO[EstadoCita.PROGRAMADA];
}

/**
 * El tema classic de FullCalendar 7 pinta con --fc-event-color. Hay que
 * aplicarlo en el elemento (y no fiarse de backgroundColor del evento).
 */
export function aplicarColorEventoCalendario(
  el: HTMLElement,
  estado: string | undefined | null
): void {
  const paleta = paletaDeEstado(estado);
  const clase = (estado ?? EstadoCita.PROGRAMADA).toString().toLowerCase();
  el.classList.add('pz-cal-evento', `pz-cal-evento--${clase}`);
  el.style.setProperty('--fc-event-color', paleta.backgroundColor);
  el.style.setProperty('--fc-event-bg-color', paleta.backgroundColor);
  el.style.setProperty('--fc-event-border-color', paleta.borderColor);
  el.style.setProperty('--fc-event-text-color', paleta.textColor);
  el.style.backgroundColor = paleta.backgroundColor;
  el.style.borderColor = paleta.borderColor;
  el.style.color = paleta.textColor;
  el.querySelectorAll<HTMLElement>('*').forEach((nodo) => {
    nodo.style.setProperty('--fc-event-color', paleta.backgroundColor);
    nodo.style.setProperty('--fc-event-bg-color', paleta.backgroundColor);
  });
}

export function citasToFullCalendarEvents(citas: readonly Cita[]): EventInput[] {
  return citas.map((cita) => {
    const paleta = paletaDeEstado(cita.estado);
    const estadoClase = (cita.estado ?? EstadoCita.PROGRAMADA).toString().toLowerCase();
    return {
      id: `cita-${cita.id}`,
      title: `${cita.pacienteNombre} · ${etiquetaEstado(cita.estado)}`,
      start: aFechaHoraIso(cita.fecha, cita.horaInicio),
      end: aFechaHoraIso(cita.fecha, cita.horaFin),
      color: paleta.backgroundColor,
      backgroundColor: paleta.backgroundColor,
      borderColor: paleta.borderColor,
      textColor: paleta.textColor,
      className: `pz-cal-evento pz-cal-evento--${estadoClase}`,
      classNames: ['pz-cal-evento', `pz-cal-evento--${estadoClase}`],
      extendedProps: {
        tipo: 'cita',
        citaId: cita.id,
        estado: cita.estado,
        medicoNombre: cita.medicoNombre,
        pacienteTelefono: cita.pacienteTelefono,
      },
    };
  });
}

function etiquetaEstado(estado: EstadoCita | string | undefined): string {
  switch (estado) {
    case EstadoCita.ATENDIDA:
    case 'ATENDIDA':
      return 'Atendida';
    case EstadoCita.CANCELADA:
    case 'CANCELADA':
      return 'Cancelada';
    default:
      return 'Programada';
  }
}

/**
 * Las franjas libres se pintan como eventos para que el paciente las vea en el
 * mismo calendario donde elegiría la hora; la seleccionada se resalta.
 */
export function slotsToFullCalendarEvents(
  slots: readonly SlotDisponible[],
  seleccionada: string | null
): EventInput[] {
  return slots.map((slot) => {
    const clave = claveSlot(slot);
    const elegido = clave === seleccionada;
    return {
      id: `slot-${clave}`,
      title: elegido ? '✓ Seleccionada' : 'Disponible',
      start: aFechaHoraIso(slot.fecha, slot.horaInicio),
      end: aFechaHoraIso(slot.fecha, slot.horaFin),
      ...(elegido ? PALETA_SLOT_ELEGIDO : PALETA_SLOT_LIBRE),
      extendedProps: {
        tipo: 'slot',
        clave,
        fecha: slot.fecha,
        horaInicio: slot.horaInicio,
        horaFin: slot.horaFin,
      },
    };
  });
}
