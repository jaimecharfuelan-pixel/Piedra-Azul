import { Observable } from 'rxjs';
import { ConfiguracionSistema } from '../models/configuracion-sistema.model';
import {
  ConfigurarPeriodoCommand,
  PeriodoDisponibilidad,
} from '../models/periodo-disponibilidad.model';
import { SlotDisponible } from '../models/slot-disponible.model';

/**
 * Puerto de salida hacia el módulo Disponibilidad del backend (RF2 y RF3).
 */
export abstract class DisponibilidadRepositoryPort {
  abstract obtenerConfiguracion(): Observable<ConfiguracionSistema>;

  abstract actualizarVentana(semanas: number): Observable<ConfiguracionSistema>;

  abstract configurarPeriodo(
    comando: ConfigurarPeriodoCommand
  ): Observable<PeriodoDisponibilidad>;

  abstract listarPeriodos(medicoId: number): Observable<PeriodoDisponibilidad[]>;

  abstract consultarSlots(medicoId: number, fecha: string): Observable<SlotDisponible[]>;

  abstract consultarSlotsPorRango(
    medicoId: number,
    desde: string,
    hasta: string
  ): Observable<SlotDisponible[]>;
}
