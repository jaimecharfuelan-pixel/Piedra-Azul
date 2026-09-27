import { Component, EventEmitter, Input, Output } from '@angular/core';
import { UiButtonComponent, VarianteBoton } from '../atoms/ui-button.component';

/**
 * Organismo: confirmación de una acción con consecuencias.
 *
 * Toda acción destructiva (cancelar o cerrar una cita) pasa por aquí para que
 * exista un paso atrás antes de ejecutarla.
 */
@Component({
  selector: 'pz-confirm-dialog',
  standalone: true,
  imports: [UiButtonComponent],
  template: `
    @if (abierto) {
      <div class="pz-dialogo__fondo" (click)="cancelado.emit()"></div>
      <div
        class="pz-dialogo"
        role="dialog"
        aria-modal="true"
        [attr.aria-labelledby]="'pz-dialogo-titulo'"
      >
        <h2 id="pz-dialogo-titulo" class="pz-dialogo__titulo">{{ titulo }}</h2>
        <p class="pz-dialogo__mensaje">{{ mensaje }}</p>
        <ng-content />
        <div class="pz-dialogo__acciones">
          <ui-button variante="fantasma" (pulsado)="cancelado.emit()">
            {{ textoCancelar }}
          </ui-button>
          <ui-button [variante]="varianteConfirmar" [cargando]="procesando" (pulsado)="confirmado.emit()">
            {{ textoConfirmar }}
          </ui-button>
        </div>
      </div>
    }
  `,
  styles: [
    `
      .pz-dialogo__fondo {
        position: fixed;
        inset: 0;
        background: rgba(12, 26, 44, 0.45);
        z-index: 40;
      }

      .pz-dialogo {
        position: fixed;
        z-index: 41;
        top: 50%;
        left: 50%;
        transform: translate(-50%, -50%);
        width: min(30rem, calc(100vw - 2rem));
        background: var(--pz-blanco);
        border-radius: var(--pz-radio-lg);
        box-shadow: var(--pz-sombra-lg);
        padding: var(--pz-esp-5);
      }

      .pz-dialogo__titulo {
        font-size: var(--pz-texto-lg);
      }

      .pz-dialogo__mensaje {
        margin: var(--pz-esp-3) 0;
        color: var(--pz-gris-700);
        font-size: var(--pz-texto-sm);
      }

      .pz-dialogo__acciones {
        display: flex;
        justify-content: flex-end;
        gap: var(--pz-esp-2);
        margin-top: var(--pz-esp-5);
      }
    `,
  ],
})
export class PzConfirmDialogComponent {
  @Input() abierto = false;
  @Input() titulo = '¿Confirmas la acción?';
  @Input() mensaje = '';
  @Input() textoConfirmar = 'Confirmar';
  @Input() textoCancelar = 'Volver';
  @Input() varianteConfirmar: VarianteBoton = 'primario';
  @Input() procesando = false;

  @Output() readonly confirmado = new EventEmitter<void>();
  @Output() readonly cancelado = new EventEmitter<void>();
}
