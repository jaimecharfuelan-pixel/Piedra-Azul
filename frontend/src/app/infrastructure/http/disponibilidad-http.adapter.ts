import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { ConfiguracionSistema } from '../../domain/models/configuracion-sistema.model';
import {
  ConfigurarPeriodoCommand,
  PeriodoDisponibilidad,
} from '../../domain/models/periodo-disponibilidad.model';
import { SlotDisponible } from '../../domain/models/slot-disponible.model';
import { DisponibilidadRepositoryPort } from '../../domain/ports/disponibilidad.repository.port';

const BASE = '/api/disponibilidad';

@Injectable()
export class DisponibilidadHttpAdapter extends DisponibilidadRepositoryPort {
  private readonly http = inject(HttpClient);

  obtenerConfiguracion(): Observable<ConfiguracionSistema> {
    return this.http.get<ConfiguracionSistema>(`${BASE}/configuracion`);
  }

  actualizarVentana(semanas: number): Observable<ConfiguracionSistema> {
    return this.http.put<ConfiguracionSistema>(`${BASE}/configuracion`, { semanas });
  }

  configurarPeriodo(comando: ConfigurarPeriodoCommand): Observable<PeriodoDisponibilidad> {
    return this.http.post<PeriodoDisponibilidad>(`${BASE}/periodos`, comando);
  }

  listarPeriodos(medicoId: number): Observable<PeriodoDisponibilidad[]> {
    return this.http.get<PeriodoDisponibilidad[]>(`${BASE}/periodos`, {
      params: { medicoId },
    });
  }

  consultarSlots(medicoId: number, fecha: string): Observable<SlotDisponible[]> {
    return this.http.get<SlotDisponible[]>(`${BASE}/slots`, {
      params: { medicoId, fecha },
    });
  }

  consultarSlotsPorRango(
    medicoId: number,
    desde: string,
    hasta: string
  ): Observable<SlotDisponible[]> {
    return this.http.get<SlotDisponible[]>(`${BASE}/slots/rango`, {
      params: { medicoId, desde, hasta },
    });
  }
}
