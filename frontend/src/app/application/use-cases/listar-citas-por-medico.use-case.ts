import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ListadoCitas } from '../../domain/models/cita.model';
import { OrdenCitas } from '../../domain/models/orden-citas.enum';
import { CitaRepositoryPort } from '../../domain/ports/cita.repository.port';

/**
 * RF1: listar las citas de un médico en una fecha, con cantidad y orden.
 */
@Injectable({ providedIn: 'root' })
export class ListarCitasPorMedicoUseCase {
  private readonly repositorio = inject(CitaRepositoryPort);

  execute(
    medicoId: number,
    fecha: string,
    orden: OrdenCitas = OrdenCitas.HORA_ASC
  ): Observable<ListadoCitas> {
    return this.repositorio.listarPorMedicoYFecha(medicoId, fecha, orden);
  }
}
