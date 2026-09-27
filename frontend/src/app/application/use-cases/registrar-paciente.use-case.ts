import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Paciente, RegistrarPacienteCommand } from '../../domain/models/paciente.model';
import { PacienteRepositoryPort } from '../../domain/ports/paciente.repository.port';

/** RF2: el paciente se registra en la web antes de agendar. */
@Injectable({ providedIn: 'root' })
export class RegistrarPacienteUseCase {
  private readonly repo = inject(PacienteRepositoryPort);

  execute(comando: RegistrarPacienteCommand): Observable<Paciente> {
    return this.repo.registrar(comando);
  }
}
