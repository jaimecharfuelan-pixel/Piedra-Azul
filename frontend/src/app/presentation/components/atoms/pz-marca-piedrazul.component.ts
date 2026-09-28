import { Component, Input } from '@angular/core';
import { RouterLink } from '@angular/router';

export type VarianteMarcaPiedrazul = 'barra' | 'auth' | 'pie';

/**
 * Marca de Piedra Azul: piedra azulada con cruz médica, no solo iniciales.
 */
@Component({
  selector: 'pz-marca-piedrazul',
  standalone: true,
  imports: [RouterLink],
  template: `
    <a
      class="pz-marca-pz"
      [class.pz-marca-pz--auth]="variante === 'auth'"
      [class.pz-marca-pz--pie]="variante === 'pie'"
      routerLink="/"
      aria-label="Piedra Azul, centro de salud"
    >
      <img
        class="pz-marca-pz__icono"
        src="/marca/piedrazul-logo.png"
        width="52"
        height="52"
        alt=""
      />
      <span class="pz-marca-pz__texto">
        <strong>Piedra Azul</strong>
        <small>Centro de salud</small>
      </span>
    </a>
  `,
  styles: [
    `
      .pz-marca-pz {
        display: inline-flex;
        align-items: center;
        gap: var(--pz-esp-3);
        text-decoration: none;
        color: var(--pz-azul-900);
        min-width: 0;
      }

      .pz-marca-pz__icono {
        width: 52px;
        height: 52px;
        object-fit: contain;
        flex-shrink: 0;
        border-radius: 12px;
        background: var(--pz-blanco);
      }

      .pz-marca-pz__texto {
        display: flex;
        flex-direction: column;
        line-height: 1.15;
      }

      .pz-marca-pz__texto strong {
        font-size: 1.05rem;
        letter-spacing: -0.02em;
      }

      .pz-marca-pz__texto small {
        color: var(--pz-azul-700);
        font-size: var(--pz-texto-xs);
        font-weight: 600;
      }

      .pz-marca-pz--auth .pz-marca-pz__icono {
        width: 56px;
        height: 56px;
      }

      .pz-marca-pz--pie {
        color: var(--pz-blanco);
      }

      .pz-marca-pz--pie .pz-marca-pz__texto small {
        color: var(--pz-azul-300);
      }
    `,
  ],
})
export class PzMarcaPiedrazulComponent {
  @Input() variante: VarianteMarcaPiedrazul = 'barra';
}
