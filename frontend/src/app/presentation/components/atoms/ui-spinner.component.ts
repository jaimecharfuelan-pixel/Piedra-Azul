import { Component, Input } from '@angular/core';

/**
 * Átomo: indicador de carga con texto asociado.
 * Hace visible el estado del sistema mientras se espera al backend.
 */
@Component({
  selector: 'ui-spinner',
  standalone: true,
  template: `
    <p class="ui-cargando" role="status" aria-live="polite">
      <span class="ui-cargando__girador" aria-hidden="true"></span>
      {{ texto }}
    </p>
  `,
  styles: [
    `
      .ui-cargando {
        display: flex;
        align-items: center;
        gap: var(--pz-esp-2);
        margin: 0;
        color: var(--pz-gris-500);
        font-size: var(--pz-texto-sm);
      }

      .ui-cargando__girador {
        width: 1rem;
        height: 1rem;
        border: 2px solid var(--pz-azul-300);
        border-right-color: transparent;
        border-radius: 50%;
        animation: ui-cargando-girar 0.8s linear infinite;
      }

      @keyframes ui-cargando-girar {
        to {
          transform: rotate(360deg);
        }
      }

      @media (prefers-reduced-motion: reduce) {
        .ui-cargando__girador {
          animation-duration: 2.5s;
        }
      }
    `,
  ],
})
export class UiSpinnerComponent {
  @Input() texto = 'Cargando…';
}
