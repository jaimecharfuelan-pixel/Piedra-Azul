import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { CrearEspecialidadCommand, Especialidad } from '../../domain/models/especialidad.model';
import { EspecialidadRepositoryPort } from '../../domain/ports/especialidad.repository.port';

@Injectable()
export class EspecialidadHttpAdapter extends EspecialidadRepositoryPort {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/personas/especialidades';

  listarActivas(): Observable<Especialidad[]> {
    return this.http.get<Especialidad[]>(this.baseUrl);
  }

  crear(comando: CrearEspecialidadCommand): Observable<Especialidad> {
    return this.http.post<Especialidad>(this.baseUrl, comando);
  }
}
