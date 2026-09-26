/**
 * Value Object de franja horaria (espejo del núcleo backend).
 * Duración mínima: 30 minutos. Usado en citas y disponibilidad (RF2/RF3).
 */
export class TimeRange {
  static readonly DURACION_MINIMA_MINUTOS = 30;

  readonly horaInicio: string;
  readonly horaFin: string;

  constructor(horaInicio: string, horaFin: string) {
    if (!horaInicio || !horaFin) {
      throw new Error('RANGO_HORARIO_INVALIDO: horaInicio y horaFin son obligatorios');
    }
    const inicio = TimeRange.parseMinutes(horaInicio);
    const fin = TimeRange.parseMinutes(horaFin);
    if (fin <= inicio) {
      throw new Error('RANGO_HORARIO_INVALIDO: la hora de fin debe ser posterior al inicio');
    }
    if (fin - inicio < TimeRange.DURACION_MINIMA_MINUTOS) {
      throw new Error(
        `DURACION_INVALIDA: mínimo ${TimeRange.DURACION_MINIMA_MINUTOS} minutos`
      );
    }
    this.horaInicio = horaInicio;
    this.horaFin = horaFin;
  }

  solapaCon(otro: TimeRange): boolean {
    const a0 = TimeRange.parseMinutes(this.horaInicio);
    const a1 = TimeRange.parseMinutes(this.horaFin);
    const b0 = TimeRange.parseMinutes(otro.horaInicio);
    const b1 = TimeRange.parseMinutes(otro.horaFin);
    return a0 < b1 && b0 < a1;
  }

  duracionMinutos(): number {
    return TimeRange.parseMinutes(this.horaFin) - TimeRange.parseMinutes(this.horaInicio);
  }

  private static parseMinutes(hhmm: string): number {
    const [h, m] = hhmm.split(':').map(Number);
    return h * 60 + m;
  }
}
