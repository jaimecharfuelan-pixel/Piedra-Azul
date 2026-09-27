import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AgendarCitaCommand,
  Cita,
  ReagendarCitaCommand,
} from '../../domain/models/cita.model';
import { Consulta } from '../../domain/models/consulta.model';
import { CitaRepositoryPort } from '../../domain/ports/cita.repository.port';

/** RF2: el paciente reserva una franja libre. */
@Injectable({ providedIn: 'root' })
export class AgendarCitaUseCase {
  private readonly repositorio = inject(CitaRepositoryPort);

  execute(comando: AgendarCitaCommand): Observable<Cita> {
    return this.repositorio.agendar(comando);
  }
}

@Injectable({ providedIn: 'root' })
export class CancelarCitaUseCase {
  private readonly repositorio = inject(CitaRepositoryPort);

  execute(citaId: number): Observable<Cita> {
    return this.repositorio.cancelar(citaId);
  }
}

@Injectable({ providedIn: 'root' })
export class ReagendarCitaUseCase {
  private readonly repositorio = inject(CitaRepositoryPort);

  execute(citaId: number, comando: ReagendarCitaCommand): Observable<Cita> {
    return this.repositorio.reagendar(citaId, comando);
  }
}

/** Sólo el médico cierra la cita; genera la entrada del historial. */
@Injectable({ providedIn: 'root' })
export class MarcarCitaAtendidaUseCase {
  private readonly repositorio = inject(CitaRepositoryPort);

  execute(citaId: number, observaciones: string): Observable<Consulta> {
    return this.repositorio.marcarAtendida(citaId, observaciones);
  }
}

/** "Mis citas" del paciente. */
@Injectable({ providedIn: 'root' })
export class ListarCitasPacienteUseCase {
  private readonly repositorio = inject(CitaRepositoryPort);

  execute(pacienteId: number): Observable<Cita[]> {
    return this.repositorio.listarPorPaciente(pacienteId);
  }
}
