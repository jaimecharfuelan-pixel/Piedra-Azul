import { Component, Input } from '@angular/core';

/**
 * Molécula: hueco vacío explicado.
 * Un listado sin resultados dice qué pasó y qué hacer, en vez de quedar en blanco.
 */
@Component({
  selector: 'ui-empty-state',
  standalone: true,
  template: `
    <div class="ui-vacio">
      <p class="ui-vacio__titulo">{{ titulo }}</p>
      @if (sugerencia) {
        <p class="ui-vacio__sugerencia">{{ sugerencia }}</p>
      }
      <ng-content />
    </div>
  `,
  styles: [
    `
      .ui-vacio {
        text-align: center;
        padding: var(--pz-esp-6) var(--pz-esp-4);
        border: 1px dashed var(--pz-gris-300);
        border-radius: var(--pz-radio);
        background: var(--pz-gris-100);
      }

      .ui-vacio__titulo {
        margin: 0;
        font-weight: 600;
        color: var(--pz-gris-700);
      }

      .ui-vacio__sugerencia {
        margin: var(--pz-esp-2) 0 0;
        font-size: var(--pz-texto-sm);
        color: var(--pz-gris-500);
      }
    `,
  ],
})
export class UiEmptyStateComponent {
  @Input({ required: true }) titulo = '';
  @Input() sugerencia = '';
}
