import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { Cita } from '../../../domain/models/cita.model';
import { EstadoCita } from '../../../domain/models/estado-cita.enum';
import {
  CampoOrdenable,
  COLUMNAS_ORDENABLES,
  esDescendente,
  OrdenCitas,
} from '../../../domain/models/orden-citas.enum';
import { UiBadgeComponent } from '../atoms/ui-badge.component';
import { UiButtonComponent } from '../atoms/ui-button.component';

/**
 * Organismo: tabla de citas con orden y acciones.
 *
 * Los encabezados ordenables son botones reales con {@code aria-sort}, así que
 * el orden se puede cambiar con teclado y los lectores de pantalla lo anuncian.
 */
@Component({
  selector: 'pz-citas-table',
  standalone: true,
  imports: [CommonModule, UiBadgeComponent, UiButtonComponent],
  templateUrl: './pz-citas-table.component.html',
  styleUrl: './pz-citas-table.component.scss',
})
export class PzCitasTableComponent {
  @Input() citas: readonly Cita[] = [];
  @Input() orden: OrdenCitas = OrdenCitas.HORA_ASC;
  @Input() mostrarAcciones = true;
  @Input() mostrarAtender = true;
  @Input() mostrarCancelarReagendar = true;
  @Input() leyenda = '';

  @Output() readonly ordenPulsado = new EventEmitter<CampoOrdenable>();
  @Output() readonly cancelarPulsado = new EventEmitter<Cita>();
  @Output() readonly reagendarPulsado = new EventEmitter<Cita>();
  @Output() readonly atenderPulsado = new EventEmitter<Cita>();

  readonly columnas = COLUMNAS_ORDENABLES;

  esProgramada(cita: Cita): boolean {
    return cita.estado === EstadoCita.PROGRAMADA;
  }

  /** Valor de aria-sort para el encabezado de una columna. */
  sentidoDe(campo: CampoOrdenable): 'ascending' | 'descending' | 'none' {
    const columna = this.columnas.find((c) => c.campo === campo);
    if (!columna || (this.orden !== columna.asc && this.orden !== columna.desc)) {
      return 'none';
    }
    return esDescendente(this.orden) ? 'descending' : 'ascending';
  }

  indicadorDe(campo: CampoOrdenable): string {
    const sentido = this.sentidoDe(campo);
    if (sentido === 'none') {
      return '↕';
    }
    return sentido === 'ascending' ? '↑' : '↓';
  }
}
