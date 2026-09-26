import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Paciente } from '../../domain/models/paciente.model';
import { PacienteRepositoryPort } from '../../domain/ports/paciente.repository.port';

@Injectable({ providedIn: 'root' })
export class ListarPacientesUseCase {
  private readonly repo = inject(PacienteRepositoryPort);

  execute(): Observable<Paciente[]> {
    return this.repo.listarTodos();
  }
}
