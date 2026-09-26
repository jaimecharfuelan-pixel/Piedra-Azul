import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Cita } from '../../domain/models/cita.model';
import { CitaRepositoryPort } from '../../domain/ports/cita.repository.port';

/** Caso de uso de aplicación (capa hexagonal). */
@Injectable({ providedIn: 'root' })
export class ListarCitasCalendarioUseCase {
  private readonly citas = inject(CitaRepositoryPort);

  execute(medicoId: string, desde: string, hasta: string): Observable<Cita[]> {
    return this.citas.listarPorMedicoYRango(medicoId, desde, hasta);
  }
}
