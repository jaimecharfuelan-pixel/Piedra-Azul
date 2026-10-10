import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CitaRepositoryPort } from '../../domain/ports/cita.repository.port';

@Injectable({ providedIn: 'root' })
export class ExportarCitasCsvUseCase {
  private readonly repo = inject(CitaRepositoryPort);

  execute(medicoId: number, fecha: string): Observable<Blob> {
    return this.repo.exportarCsv(medicoId, fecha);
  }
}
