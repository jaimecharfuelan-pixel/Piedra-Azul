import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';

export type VarianteBoton = 'primario' | 'secundario' | 'peligro' | 'fantasma';
export type TamanoBoton = 'md' | 'sm';

/**
 * Átomo: botón del sistema de diseño.
 *
 * Mientras está ocupado se deshabilita y muestra un indicador, para que el
 * usuario sepa que la acción está en curso y no la dispare dos veces.
 */
@Component({
  selector: 'ui-button',
  standalone: true,
  imports: [CommonModule],
  template: `
    <button
      [type]="type"
      [disabled]="disabled || cargando"
      [attr.aria-busy]="cargando"
      [attr.aria-label]="ariaLabel"
      [ngClass]="['ui-boton', 'ui-boton--' + variante, 'ui-boton--' + tamano]"
      (click)="pulsado.emit()"
    >
      @if (cargando) {
        <span class="ui-boton__girador" aria-hidden="true"></span>
      }
      <ng-content />
    </button>
  `,
  styles: [
    `
      :host {
        display: inline-flex;
      }

      .ui-boton {
        display: inline-flex;
        align-items: center;
        justify-content: center;
        gap: var(--pz-esp-2);
        font: inherit;
        font-weight: 600;
        border-radius: var(--pz-radio);
        border: 1px solid transparent;
        cursor: pointer;
        transition: background var(--pz-transicion), border-color var(--pz-transicion),
          color var(--pz-transicion);
        white-space: nowrap;
      }

      .ui-boton--md {
        padding: var(--pz-esp-2) var(--pz-esp-4);
      }

      .ui-boton--sm {
        padding: var(--pz-esp-1) var(--pz-esp-3);
        font-size: var(--pz-texto-sm);
      }

      .ui-boton:disabled {
        opacity: 0.55;
        cursor: not-allowed;
      }

      .ui-boton--primario {
        background: var(--pz-azul-700);
        color: var(--pz-blanco);
      }

      .ui-boton--primario:hover:not(:disabled) {
        background: var(--pz-azul-900);
      }

      .ui-boton--secundario {
        background: var(--pz-blanco);
        color: var(--pz-azul-700);
        border-color: var(--pz-azul-300);
      }

      .ui-boton--secundario:hover:not(:disabled) {
        background: var(--pz-azul-50);
      }

      .ui-boton--peligro {
        background: var(--pz-blanco);
        color: var(--pz-error-fuerte);
        border-color: var(--pz-error-fuerte);
      }

      .ui-boton--peligro:hover:not(:disabled) {
        background: var(--pz-error-suave);
      }

      .ui-boton--fantasma {
        background: transparent;
        color: var(--pz-gris-700);
      }

      .ui-boton--fantasma:hover:not(:disabled) {
        background: var(--pz-gris-100);
      }

      .ui-boton__girador {
        width: 0.85em;
        height: 0.85em;
        border: 2px solid currentColor;
        border-right-color: transparent;
        border-radius: 50%;
        animation: ui-boton-girar 0.7s linear infinite;
      }

      @keyframes ui-boton-girar {
        to {
          transform: rotate(360deg);
        }
      }

      @media (prefers-reduced-motion: reduce) {
        .ui-boton__girador {
          animation-duration: 2s;
        }
      }
    `,
  ],
})
export class UiButtonComponent {
  @Input() variante: VarianteBoton = 'primario';
  @Input() tamano: TamanoBoton = 'md';
  @Input() type: 'button' | 'submit' | 'reset' = 'button';
  @Input() disabled = false;
  @Input() cargando = false;
  @Input() ariaLabel: string | null = null;

  @Output() readonly pulsado = new EventEmitter<void>();
}
