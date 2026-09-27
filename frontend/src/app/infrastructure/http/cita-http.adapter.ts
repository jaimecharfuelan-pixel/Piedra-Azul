import { HttpClient } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import {
  AgendarCitaCommand,
  Cita,
  ListadoCitas,
  ReagendarCitaCommand,
} from '../../domain/models/cita.model';
import { Consulta } from '../../domain/models/consulta.model';
import { OrdenCitas } from '../../domain/models/orden-citas.enum';
import { CitaRepositoryPort } from '../../domain/ports/cita.repository.port';

const BASE = '/api/citas';

/**
 * Adaptador REST del módulo Citas. No transforma el payload: el backend ya
 * entrega fechas 'yyyy-MM-dd' y horas 'HH:mm', que es lo que usa la vista.
 */
@Injectable()
export class CitaHttpAdapter extends CitaRepositoryPort {
  private readonly http = inject(HttpClient);

  listarPorMedicoYFecha(
    medicoId: number,
    fecha: string,
    orden: OrdenCitas
  ): Observable<ListadoCitas> {
    return this.http.get<ListadoCitas>(BASE, {
      params: { medicoId, fecha, orden },
    });
  }

  listarPorMedicoYRango(medicoId: number, desde: string, hasta: string): Observable<Cita[]> {
    return this.http.get<Cita[]>(`${BASE}/rango`, {
      params: { medicoId, desde, hasta },
    });
  }

  listarPorPaciente(pacienteId: number): Observable<Cita[]> {
    return this.http.get<Cita[]>(`${BASE}/paciente/${pacienteId}`);
  }

  agendar(comando: AgendarCitaCommand): Observable<Cita> {
    return this.http.post<Cita>(BASE, comando);
  }

  cancelar(citaId: number): Observable<Cita> {
    return this.http.put<Cita>(`${BASE}/${citaId}/cancelacion`, {});
  }

  reagendar(citaId: number, comando: ReagendarCitaCommand): Observable<Cita> {
    return this.http.put<Cita>(`${BASE}/${citaId}/reagendamiento`, comando);
  }

  marcarAtendida(citaId: number, observaciones: string): Observable<Consulta> {
    return this.http.put<Consulta>(`${BASE}/${citaId}/atencion`, { observaciones });
  }

  historialPorPaciente(pacienteId: number): Observable<Consulta[]> {
    return this.http.get<Consulta[]>(`${BASE}/historial/paciente/${pacienteId}`);
  }

  historialPorMedico(medicoId: number): Observable<Consulta[]> {
    return this.http.get<Consulta[]>(`${BASE}/historial/medico/${medicoId}`);
  }
}
