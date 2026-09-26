import { Observable } from 'rxjs';
import { Cita } from '../models/cita.model';

/** Puerto de salida: el dominio no conoce HTTP ni FullCalendar. */
export abstract class CitaRepositoryPort {
  abstract listarPorMedicoYRango(
    medicoId: string,
    desde: string,
    hasta: string
  ): Observable<Cita[]>;
}
