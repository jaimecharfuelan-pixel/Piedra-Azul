import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { Consulta } from '../../domain/models/consulta.model';
import { CitaRepositoryPort } from '../../domain/ports/cita.repository.port';

@Injectable({ providedIn: 'root' })
export class HistorialPorPacienteUseCase {
  private readonly repositorio = inject(CitaRepositoryPort);

  execute(pacienteId: number): Observable<Consulta[]> {
    return this.repositorio.historialPorPaciente(pacienteId);
  }
}

@Injectable({ providedIn: 'root' })
export class HistorialPorMedicoUseCase {
  private readonly repositorio = inject(CitaRepositoryPort);

  execute(medicoId: number): Observable<Consulta[]> {
    return this.repositorio.historialPorMedico(medicoId);
  }
}
