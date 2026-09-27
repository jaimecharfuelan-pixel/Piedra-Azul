import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { SlotDisponible } from '../../domain/models/slot-disponible.model';
import { DisponibilidadRepositoryPort } from '../../domain/ports/disponibilidad.repository.port';

/** RF2: franjas libres de un médico en un día. */
@Injectable({ providedIn: 'root' })
export class ConsultarSlotsUseCase {
  private readonly repositorio = inject(DisponibilidadRepositoryPort);

  execute(medicoId: number, fecha: string): Observable<SlotDisponible[]> {
    return this.repositorio.consultarSlots(medicoId, fecha);
  }
}

/** RF2: franjas libres de una semana completa, para pintar el calendario. */
@Injectable({ providedIn: 'root' })
export class ConsultarSlotsPorRangoUseCase {
  private readonly repositorio = inject(DisponibilidadRepositoryPort);

  execute(medicoId: number, desde: string, hasta: string): Observable<SlotDisponible[]> {
    return this.repositorio.consultarSlotsPorRango(medicoId, desde, hasta);
  }
}
