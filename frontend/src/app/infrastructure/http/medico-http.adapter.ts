import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  ActualizarMedicoCommand,
  CrearMedicoCommand,
  Medico,
} from '../../domain/models/medico.model';
import { MedicoRepositoryPort } from '../../domain/ports/medico.repository.port';

@Injectable()
export class MedicoHttpAdapter extends MedicoRepositoryPort {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/personas/medicos';

  listarActivos(): Observable<Medico[]> {
    return this.http.get<Medico[]>(this.baseUrl);
  }

  buscarPorId(id: number): Observable<Medico> {
    return this.http.get<Medico>(`${this.baseUrl}/${id}`);
  }

  crear(comando: CrearMedicoCommand): Observable<Medico> {
    return this.http.post<Medico>(this.baseUrl, comando);
  }

  actualizar(id: number, comando: ActualizarMedicoCommand): Observable<Medico> {
    return this.http.put<Medico>(`${this.baseUrl}/${id}`, comando);
  }
}
