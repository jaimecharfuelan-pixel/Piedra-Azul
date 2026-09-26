import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Especialidad, CrearEspecialidadCommand } from '../../domain/models/especialidad.model';
import { EspecialidadRepositoryPort } from '../../domain/ports/especialidad.repository.port';

@Injectable({ providedIn: 'root' })
export class ListarEspecialidadesUseCase {
  private readonly repo = inject(EspecialidadRepositoryPort);

  execute(): Observable<Especialidad[]> {
    return this.repo.listarActivas();
  }
}

@Injectable({ providedIn: 'root' })
export class CrearEspecialidadUseCase {
  private readonly repo = inject(EspecialidadRepositoryPort);

  execute(comando: CrearEspecialidadCommand): Observable<Especialidad> {
    return this.repo.crear(comando);
  }
}
