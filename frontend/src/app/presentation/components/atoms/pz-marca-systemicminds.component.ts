import { Component, Input } from '@angular/core';

/** Marca de SystemicMinds Solutions Inc. */
@Component({
  selector: 'pz-marca-systemicminds',
  standalone: true,
  template: `
    <div
      class="pz-marca-sm"
      [class.pz-marca-sm--compacta]="compacta"
      [class.pz-marca-sm--clara]="clara"
    >
      <img
        class="pz-marca-sm__icono"
        src="/marca/systemicminds-logo.png"
        width="48"
        height="48"
        alt=""
      />
      <span class="pz-marca-sm__texto">
        <strong>SystemicMinds</strong>
        <small>Solutions Inc.</small>
      </span>
    </div>
  `,
  styles: [
    `
      .pz-marca-sm {
        display: inline-flex;
        align-items: center;
        gap: var(--pz-esp-3);
        color: var(--pz-gris-900);
      }

      .pz-marca-sm__icono {
        width: 48px;
        height: 48px;
        object-fit: contain;
        flex-shrink: 0;
        border-radius: 12px;
        background: var(--pz-blanco);
      }

      .pz-marca-sm__texto {
        display: flex;
        flex-direction: column;
        line-height: 1.15;
      }

      .pz-marca-sm__texto strong {
        font-size: 1rem;
        letter-spacing: -0.03em;
      }

      .pz-marca-sm__texto small {
        color: var(--pz-gris-500);
        font-size: var(--pz-texto-xs);
        font-weight: 600;
      }

      .pz-marca-sm--clara {
        color: var(--pz-blanco);
      }

      .pz-marca-sm--clara .pz-marca-sm__texto small {
        color: var(--pz-azul-300);
      }

      .pz-marca-sm--compacta .pz-marca-sm__icono {
        width: 40px;
        height: 40px;
      }
    `,
  ],
})
export class PzMarcaSystemicmindsComponent {
  @Input() compacta = false;
  @Input() clara = false;
}
