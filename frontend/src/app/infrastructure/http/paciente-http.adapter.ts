import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { Paciente, RegistrarPacienteCommand } from '../../domain/models/paciente.model';
import { PacienteRepositoryPort } from '../../domain/ports/paciente.repository.port';

@Injectable()
export class PacienteHttpAdapter extends PacienteRepositoryPort {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/personas/pacientes';

  listarTodos(): Observable<Paciente[]> {
    return this.http.get<Paciente[]>(this.baseUrl);
  }

  buscarPorId(id: number): Observable<Paciente> {
    return this.http.get<Paciente>(`${this.baseUrl}/${id}`);
  }

  registrar(comando: RegistrarPacienteCommand): Observable<Paciente> {
    return this.http.post<Paciente>(this.baseUrl, comando);
  }
}
