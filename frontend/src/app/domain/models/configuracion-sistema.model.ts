/**
 * Configuración global de agendamiento. Incluye límites de fecha calculados
 * por el backend.
 */
export interface ConfiguracionSistema {
  readonly id: number;
  readonly ventanaSemanas: number;
  readonly agendamientoDesde: string;
  readonly agendamientoHasta: string;
}

export const VENTANA_MINIMA_SEMANAS = 1;
export const VENTANA_MAXIMA_SEMANAS = 52;
