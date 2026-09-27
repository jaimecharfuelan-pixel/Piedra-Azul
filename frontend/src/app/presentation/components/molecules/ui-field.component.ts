import { CommonModule } from '@angular/common';
import { Component, Input } from '@angular/core';

/**
 * Molécula: etiqueta + control + ayuda + error de un campo de formulario.
 *
 * El control se proyecta con {@code <ng-content>}, así que sirve igual para
 * input, select o textarea. El id se repite en {@code for}, en
 * {@code aria-describedby} y en el mensaje de error para que el lector de
 * pantalla relacione las tres partes.
 */
@Component({
  selector: 'ui-field',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="ui-campo">
      <label class="ui-campo__etiqueta" [attr.for]="controlId">
        {{ etiqueta }}
        @if (requerido) {
          <span class="ui-campo__requerido" aria-hidden="true">*</span>
          <span class="pz-solo-lectores">(obligatorio)</span>
        }
      </label>

      <ng-content />

      @if (ayuda && !error) {
        <p class="ui-campo__ayuda" [id]="controlId + '-ayuda'">{{ ayuda }}</p>
      }
      @if (error) {
        <p class="ui-campo__error" [id]="controlId + '-error'" role="alert">{{ error }}</p>
      }
    </div>
  `,
  styles: [
    `
      .ui-campo {
        display: flex;
        flex-direction: column;
        gap: var(--pz-esp-1);
      }

      .ui-campo__etiqueta {
        font-size: var(--pz-texto-sm);
        font-weight: 600;
        color: var(--pz-gris-700);
      }

      .ui-campo__requerido {
        color: var(--pz-error-fuerte);
      }

      .ui-campo__ayuda {
        margin: 0;
        font-size: var(--pz-texto-xs);
        color: var(--pz-gris-500);
      }

      .ui-campo__error {
        margin: 0;
        font-size: var(--pz-texto-xs);
        color: var(--pz-error-fuerte);
        font-weight: 600;
      }
    `,
  ],
})
export class UiFieldComponent {
  @Input({ required: true }) etiqueta = '';
  @Input({ required: true }) controlId = '';
  @Input() ayuda = '';
  @Input() error: string | null = null;
  @Input() requerido = false;
}
