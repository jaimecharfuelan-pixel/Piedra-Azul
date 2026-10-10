import { Injectable, inject } from '@angular/core';
import { Observable, forkJoin } from 'rxjs';
import { HistorialCitasPorCedula } from '../../domain/models/historial-citas-cedula.model';
import { Paciente } from '../../domain/models/paciente.model';
import { CitaRepositoryPort } from '../../domain/ports/cita.repository.port';
import { PacienteRepositoryPort } from '../../domain/ports/paciente.repository.port';

/** Par de endpoints sin JWT: paciente por cédula + historial de citas. */
export interface ConsultaPorCedulaResultado {
  paciente: Paciente;
  historial: HistorialCitasPorCedula;
}

@Injectable({ providedIn: 'root' })
export class BuscarPacientePorCedulaUseCase {
  private readonly repo = inject(PacienteRepositoryPort);

  execute(cedula: string): Observable<Paciente> {
    return this.repo.buscarPorCedula(cedula);
  }
}

@Injectable({ providedIn: 'root' })
export class HistorialCitasPorCedulaUseCase {
  private readonly repo = inject(CitaRepositoryPort);

  execute(cedula: string): Observable<HistorialCitasPorCedula> {
    return this.repo.listarPorCedula(cedula);
  }
}

@Injectable({ providedIn: 'root' })
export class ConsultarPorCedulaUseCase {
  private readonly pacientes = inject(PacienteRepositoryPort);
  private readonly citas = inject(CitaRepositoryPort);

  execute(cedula: string): Observable<ConsultaPorCedulaResultado> {
    const normalizada = cedula.trim().replace(/[.\s-]/g, '');
    return forkJoin({
      paciente: this.pacientes.buscarPorCedula(normalizada),
      historial: this.citas.listarPorCedula(normalizada),
    });
  }
}
