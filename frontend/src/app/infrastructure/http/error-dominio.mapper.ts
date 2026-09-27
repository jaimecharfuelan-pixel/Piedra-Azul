import { HttpErrorResponse } from '@angular/common/http';
import { ErrorDominio } from '../../domain/models/error-dominio.model';

/**
 * Mensajes de apoyo por código de negocio: explican qué hacer, no sólo qué falló
 * (heurística de Nielsen "ayuda a reconocer, diagnosticar y recuperarse de errores").
 */
const SUGERENCIAS: Readonly<Record<string, string>> = {
  SLOT_NO_DISPONIBLE: 'Vuelve a cargar las franjas y elige otra hora.',
  MEDICO_NO_DISPONIBLE: 'Configura el horario del médico en la pantalla de Configuración.',
  FUERA_DE_VENTANA_AGENDAMIENTO: 'Elige una fecha dentro de la ventana habilitada.',
  CITA_NO_MODIFICABLE: 'Solo las citas programadas se pueden cancelar, reagendar o atender.',
  PERIODO_SOLAPADO: 'Usa una fecha de inicio posterior al horario que ya está vigente.',
  DURACION_CITA_INVALIDA: 'La duración debe ser de al menos 30 minutos y caber en la franja.',
  DATOS_INVALIDOS: 'Revisa los campos marcados del formulario.',
  SIN_CONEXION: 'Comprueba que el backend esté arriba en el puerto 8080.',
};

const GENERICO: ErrorDominio = {
  codigo: 'ERROR_DESCONOCIDO',
  mensaje: 'Ocurrió un error inesperado. Inténtalo de nuevo.',
};

/**
 * Traduce cualquier fallo HTTP al ErrorDominio que consumen las páginas.
 */
export function aErrorDominio(error: unknown): ErrorDominio {
  if (!(error instanceof HttpErrorResponse)) {
    return GENERICO;
  }

  if (error.status === 0) {
    return {
      codigo: 'SIN_CONEXION',
      mensaje: `No se pudo contactar el servidor. ${SUGERENCIAS['SIN_CONEXION']}`,
      status: 0,
    };
  }

  const cuerpo = error.error as
    | { codigo?: string; title?: string; detail?: string; extra?: Record<string, string> }
    | string
    | null;

  if (!cuerpo || typeof cuerpo === 'string') {
    return { ...GENERICO, status: error.status };
  }

  const codigo = cuerpo.codigo ?? cuerpo.title ?? GENERICO.codigo;
  const detalle = cuerpo.detail ?? GENERICO.mensaje;
  const sugerencia = SUGERENCIAS[codigo];

  return {
    codigo,
    mensaje: sugerencia ? `${detalle} ${sugerencia}` : detalle,
    status: error.status,
    campos: cuerpo.extra && Object.keys(cuerpo.extra).length > 0 ? cuerpo.extra : undefined,
  };
}
