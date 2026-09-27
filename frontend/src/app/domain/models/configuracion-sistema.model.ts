/**
 * Configuración global de agendamiento (RF3). El backend calcula los límites
 * de fecha para que el formulario pueda acotar el selector de día.
 */
export interface ConfiguracionSistema {
  readonly id: number;
  readonly ventanaSemanas: number;
  readonly agendamientoDesde: string;
  readonly agendamientoHasta: string;
}

export const VENTANA_MINIMA_SEMANAS = 1;
export const VENTANA_MAXIMA_SEMANAS = 52;
