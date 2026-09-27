import { Observable } from 'rxjs';
import {
  AgendarCitaCommand,
  Cita,
  ListadoCitas,
  ReagendarCitaCommand,
} from '../models/cita.model';
import { Consulta } from '../models/consulta.model';
import { OrdenCitas } from '../models/orden-citas.enum';

/**
 * Puerto de salida hacia el módulo Citas del backend.
 * La clase abstracta hace de token de inyección; el adaptador HTTP la extiende.
 */
export abstract class CitaRepositoryPort {
  /** RF1: citas de un médico en una fecha, con cantidad y orden configurable. */
  abstract listarPorMedicoYFecha(
    medicoId: number,
    fecha: string,
    orden: OrdenCitas
  ): Observable<ListadoCitas>;

  /** Citas de un médico en un rango, para la vista de calendario. */
  abstract listarPorMedicoYRango(
    medicoId: number,
    desde: string,
    hasta: string
  ): Observable<Cita[]>;

  abstract listarPorPaciente(pacienteId: number): Observable<Cita[]>;

  abstract agendar(comando: AgendarCitaCommand): Observable<Cita>;

  abstract cancelar(citaId: number): Observable<Cita>;

  abstract reagendar(citaId: number, comando: ReagendarCitaCommand): Observable<Cita>;

  abstract marcarAtendida(citaId: number, observaciones: string): Observable<Consulta>;

  abstract historialPorPaciente(pacienteId: number): Observable<Consulta[]>;

  abstract historialPorMedico(medicoId: number): Observable<Consulta[]>;
}
