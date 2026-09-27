import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ConfiguracionSistema } from '../../domain/models/configuracion-sistema.model';
import {
  ConfigurarPeriodoCommand,
  PeriodoDisponibilidad,
} from '../../domain/models/periodo-disponibilidad.model';
import { DisponibilidadRepositoryPort } from '../../domain/ports/disponibilidad.repository.port';

/** RF3: lectura de la configuración global de agendamiento. */
@Injectable({ providedIn: 'root' })
export class ObtenerConfiguracionUseCase {
  private readonly repositorio = inject(DisponibilidadRepositoryPort);

  execute(): Observable<ConfiguracionSistema> {
    return this.repositorio.obtenerConfiguracion();
  }
}

/** RF3: ventana de tiempo (en semanas) durante la que se habilitan las citas. */
@Injectable({ providedIn: 'root' })
export class ActualizarVentanaAgendamientoUseCase {
  private readonly repositorio = inject(DisponibilidadRepositoryPort);

  execute(semanas: number): Observable<ConfiguracionSistema> {
    return this.repositorio.actualizarVentana(semanas);
  }
}

/** RF3: días, franja horaria, duración de la cita y descanso entre citas. */
@Injectable({ providedIn: 'root' })
export class ConfigurarPeriodoDisponibilidadUseCase {
  private readonly repositorio = inject(DisponibilidadRepositoryPort);

  execute(comando: ConfigurarPeriodoCommand): Observable<PeriodoDisponibilidad> {
    return this.repositorio.configurarPeriodo(comando);
  }
}

/** RF3: historial de horarios del médico, para ver qué está vigente. */
@Injectable({ providedIn: 'root' })
export class ListarPeriodosDisponibilidadUseCase {
  private readonly repositorio = inject(DisponibilidadRepositoryPort);

  execute(medicoId: number): Observable<PeriodoDisponibilidad[]> {
    return this.repositorio.listarPeriodos(medicoId);
  }
}
