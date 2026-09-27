/**
 * Error de negocio ya traducido a lenguaje de la vista.
 * Corresponde al ProblemDetail que devuelve el backend (código + detalle).
 */
export interface ErrorDominio {
  readonly codigo: string;
  readonly mensaje: string;
  readonly status?: number;
  /** Errores campo a campo cuando el backend responde DATOS_INVALIDOS. */
  readonly campos?: Readonly<Record<string, string>>;
}
