import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Cita } from '../../domain/models/cita.model';
import { CitaRepositoryPort } from '../../domain/ports/cita.repository.port';

/**
 * Citas de un médico en un rango de fechas, para la vista de calendario.
 */
@Injectable({ providedIn: 'root' })
export class ListarCitasCalendarioUseCase {
  private readonly repositorio = inject(CitaRepositoryPort);

  execute(medicoId: number, desde: string, hasta: string): Observable<Cita[]> {
    return this.repositorio.listarPorMedicoYRango(medicoId, desde, hasta);
  }
}
