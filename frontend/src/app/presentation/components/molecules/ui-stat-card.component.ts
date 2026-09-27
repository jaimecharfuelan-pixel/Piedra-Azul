import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';
import { TonoBadge } from '../atoms/ui-badge.component';

/** Tarjeta con cifra destacada (resúmenes de agenda). */
@Component({
  selector: 'ui-stat-card',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div [ngClass]="['ui-cifra', 'ui-cifra--' + tono]">
      <span class="ui-cifra__valor">{{ valor }}</span>
      <span class="ui-cifra__etiqueta">{{ etiqueta }}</span>
    </div>
  `,
  styles: [
    `
      :host {
        display: block;
      }

      .ui-cifra {
        display: flex;
        flex-direction: column;
        gap: var(--pz-esp-1);
        padding: var(--pz-esp-4);
        border-radius: var(--pz-radio);
        background: var(--pz-blanco);
        border: var(--pz-borde);
        border-top: 3px solid var(--pz-gris-300);
      }

      .ui-cifra--info {
        border-top-color: var(--pz-info-fuerte);
      }

      .ui-cifra--exito {
        border-top-color: var(--pz-exito-fuerte);
      }

      .ui-cifra--error {
        border-top-color: var(--pz-error-fuerte);
      }

      .ui-cifra--alerta {
        border-top-color: var(--pz-alerta-fuerte);
      }

      .ui-cifra__valor {
        font-size: var(--pz-texto-2xl);
        font-weight: 700;
        line-height: 1;
        color: var(--pz-azul-900);
      }

      .ui-cifra__etiqueta {
        font-size: var(--pz-texto-xs);
        text-transform: uppercase;
        letter-spacing: 0.05em;
        color: var(--pz-gris-500);
      }
    `,
  ],
})
export class UiStatCardComponent {
  @Input({ required: true }) etiqueta = '';
  @Input({ required: true }) valor: number | string = 0;
  @Input() tono: TonoBadge = 'neutro';
}
