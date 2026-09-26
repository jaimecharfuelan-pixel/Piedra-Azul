import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { CrearMedicoCommand, Medico } from '../../domain/models/medico.model';
import { MedicoRepositoryPort } from '../../domain/ports/medico.repository.port';

@Injectable({ providedIn: 'root' })
export class ListarMedicosUseCase {
  private readonly repo = inject(MedicoRepositoryPort);

  execute(): Observable<Medico[]> {
    return this.repo.listarActivos();
  }
}

@Injectable({ providedIn: 'root' })
export class CrearMedicoUseCase {
  private readonly repo = inject(MedicoRepositoryPort);

  execute(comando: CrearMedicoCommand): Observable<Medico> {
    return this.repo.crear(comando);
  }
}
