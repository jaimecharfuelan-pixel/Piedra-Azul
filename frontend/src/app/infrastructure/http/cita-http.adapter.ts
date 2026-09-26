import { Injectable, inject } from '@angular/core';
import { HttpClient, HttpParams } from '@angular/common/http';
import { Observable, of } from 'rxjs';
import { catchError } from 'rxjs/operators';
import { Cita } from '../../domain/models/cita.model';
import { CitaRepositoryPort } from '../../domain/ports/cita.repository.port';

/** Adaptador secundario: HTTP hacia el backend Spring Boot. */
@Injectable()
export class CitaHttpAdapter extends CitaRepositoryPort {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/citas';

  listarPorMedicoYRango(
    medicoId: string,
    desde: string,
    hasta: string
  ): Observable<Cita[]> {
    const params = new HttpParams()
      .set('medicoId', medicoId)
      .set('desde', desde)
      .set('hasta', hasta);

    return this.http.get<Cita[]>(this.baseUrl, { params }).pipe(
      catchError(() =>
        of([
          {
            id: 'demo-1',
            medicoId,
            pacienteId: 'p-1',
            inicio: `${desde}T09:00:00`,
            fin: `${desde}T09:30:00`,
            estado: 'PROGRAMADA',
            titulo: 'Cita demo (backend aún no disponible)',
          } satisfies Cita,
        ])
      )
    );
  }
}
