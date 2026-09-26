import { EventInput } from '@fullcalendar/angular';
import { Cita } from '../../domain/models/cita.model';

/** Adaptador: mapa dominio → eventos FullCalendar. */
export function citasToFullCalendarEvents(citas: Cita[]): EventInput[] {
  return citas.map((cita) => ({
    id: cita.id,
    title: cita.titulo ?? `Cita ${cita.estado}`,
    start: cita.inicio,
    end: cita.fin,
    extendedProps: {
      medicoId: cita.medicoId,
      pacienteId: cita.pacienteId,
      estado: cita.estado,
    },
  }));
}
