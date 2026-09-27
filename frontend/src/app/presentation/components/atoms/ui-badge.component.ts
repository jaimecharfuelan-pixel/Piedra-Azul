import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { EstadoCita } from '../../../domain/models/estado-cita.enum';

export type TonoBadge = 'neutro' | 'exito' | 'error' | 'alerta' | 'info';

const TONO_POR_ESTADO: Readonly<Record<EstadoCita, TonoBadge>> = {
  [EstadoCita.PROGRAMADA]: 'info',
  [EstadoCita.ATENDIDA]: 'exito',
  [EstadoCita.CANCELADA]: 'error',
};

const TEXTO_POR_ESTADO: Readonly<Record<EstadoCita, string>> = {
  [EstadoCita.PROGRAMADA]: 'Programada',
  [EstadoCita.ATENDIDA]: 'Atendida',
  [EstadoCita.CANCELADA]: 'Cancelada',
};

/**
 * Átomo: etiqueta de estado.
 *
 * El estado se comunica con color **y** con texto, para que también se distinga
 * sin percibir el color.
 */
@Component({
  selector: 'ui-badge',
  standalone: true,
  imports: [CommonModule],
  template: `
    <span [ngClass]="['ui-badge', 'ui-badge--' + tonoEfectivo]">
      {{ textoEfectivo }}
    </span>
  `,
  styles: [
    `
      .ui-badge {
        display: inline-block;
        padding: 0.15rem var(--pz-esp-3);
        border-radius: var(--pz-radio-full);
        font-size: var(--pz-texto-xs);
        font-weight: 600;
        letter-spacing: 0.02em;
      }

      .ui-badge--neutro {
        background: var(--pz-gris-100);
        color: var(--pz-gris-700);
      }

      .ui-badge--info {
        background: var(--pz-info-suave);
        color: var(--pz-info-fuerte);
      }

      .ui-badge--exito {
        background: var(--pz-exito-suave);
        color: var(--pz-exito-fuerte);
      }

      .ui-badge--error {
        background: var(--pz-error-suave);
        color: var(--pz-error-fuerte);
      }

      .ui-badge--alerta {
        background: var(--pz-alerta-suave);
        color: var(--pz-alerta-fuerte);
      }
    `,
  ],
})
export class UiBadgeComponent {
  /** Si se pasa un estado de cita, el tono y el texto se derivan de él. */
  @Input() estado: EstadoCita | null = null;
  @Input() tono: TonoBadge = 'neutro';
  @Input() texto = '';

  get tonoEfectivo(): TonoBadge {
    return this.estado ? TONO_POR_ESTADO[this.estado] : this.tono;
  }

  get textoEfectivo(): string {
    return this.estado ? TEXTO_POR_ESTADO[this.estado] : this.texto;
  }
}
