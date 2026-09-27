import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

export type TipoAlerta = 'error' | 'exito' | 'info' | 'alerta';

/**
 * Átomo: mensaje del sistema.
 *
 * Los errores se anuncian con {@code role="alert"} para que los lectores de
 * pantalla los lean en cuanto aparecen; el resto usa una región discreta.
 */
@Component({
  selector: 'ui-alert',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div
      [ngClass]="['ui-alerta', 'ui-alerta--' + tipo]"
      [attr.role]="tipo === 'error' ? 'alert' : 'status'"
      aria-live="polite"
    >
      <strong class="ui-alerta__titulo">{{ titulo || tituloPorDefecto }}</strong>
      <p class="ui-alerta__texto">{{ mensaje }}</p>
      <ng-content />
    </div>
  `,
  styles: [
    `
      .ui-alerta {
        border-radius: var(--pz-radio);
        padding: var(--pz-esp-3) var(--pz-esp-4);
        border-left: 4px solid;
      }

      .ui-alerta__titulo {
        display: block;
        font-size: var(--pz-texto-sm);
      }

      .ui-alerta__texto {
        margin: var(--pz-esp-1) 0 0;
        font-size: var(--pz-texto-sm);
      }

      .ui-alerta--error {
        background: var(--pz-error-suave);
        color: var(--pz-error-fuerte);
        border-left-color: var(--pz-error-fuerte);
      }

      .ui-alerta--exito {
        background: var(--pz-exito-suave);
        color: var(--pz-exito-fuerte);
        border-left-color: var(--pz-exito-fuerte);
      }

      .ui-alerta--info {
        background: var(--pz-info-suave);
        color: var(--pz-info-fuerte);
        border-left-color: var(--pz-info-fuerte);
      }

      .ui-alerta--alerta {
        background: var(--pz-alerta-suave);
        color: var(--pz-alerta-fuerte);
        border-left-color: var(--pz-alerta-fuerte);
      }
    `,
  ],
})
export class UiAlertComponent {
  @Input() tipo: TipoAlerta = 'info';
  @Input() titulo = '';
  @Input() mensaje = '';

  get tituloPorDefecto(): string {
    switch (this.tipo) {
      case 'error':
        return 'No se pudo completar la acción';
      case 'exito':
        return 'Listo';
      case 'alerta':
        return 'Ten en cuenta';
      default:
        return 'Información';
    }
  }
}
