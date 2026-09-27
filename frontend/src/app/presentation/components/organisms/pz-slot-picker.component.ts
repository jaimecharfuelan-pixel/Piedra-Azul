import { CommonModule } from '@angular/common';
import { Component, EventEmitter, Input, Output } from '@angular/core';
import { claveSlot, SlotDisponible } from '../../../domain/models/slot-disponible.model';

/**
 * Organismo: rejilla de franjas libres para elegir una (RF2).
 *
 * Cada franja es un botón con {@code aria-pressed}, de modo que la selección se
 * puede hacer con teclado y queda anunciada.
 */
@Component({
  selector: 'pz-slot-picker',
  standalone: true,
  imports: [CommonModule],
  template: `
    <div class="pz-slots" role="group" [attr.aria-label]="'Franjas disponibles'">
      @for (slot of slots; track claveDe(slot)) {
        <button
          type="button"
          class="pz-slots__franja"
          [class.pz-slots__franja--activa]="claveDe(slot) === seleccionada"
          [attr.aria-pressed]="claveDe(slot) === seleccionada"
          (click)="slotElegido.emit(slot)"
        >
          <span class="pz-slots__hora">{{ slot.horaInicio }}</span>
          <span class="pz-slots__fin">a {{ slot.horaFin }}</span>
        </button>
      }
    </div>
  `,
  styles: [
    `
      .pz-slots {
        display: grid;
        grid-template-columns: repeat(auto-fill, minmax(7.5rem, 1fr));
        gap: var(--pz-esp-2);
      }

      .pz-slots__franja {
        display: flex;
        flex-direction: column;
        align-items: center;
        gap: 2px;
        padding: var(--pz-esp-3) var(--pz-esp-2);
        font: inherit;
        background: var(--pz-blanco);
        border: 1px solid var(--pz-gris-300);
        border-radius: var(--pz-radio);
        cursor: pointer;
        transition: border-color var(--pz-transicion), background var(--pz-transicion);
      }

      .pz-slots__franja:hover {
        border-color: var(--pz-azul-500);
        background: var(--pz-azul-50);
      }

      .pz-slots__franja--activa {
        background: var(--pz-azul-700);
        border-color: var(--pz-azul-900);
        color: var(--pz-blanco);
      }

      .pz-slots__hora {
        font-weight: 700;
        font-variant-numeric: tabular-nums;
      }

      .pz-slots__fin {
        font-size: var(--pz-texto-xs);
        opacity: 0.8;
      }
    `,
  ],
})
export class PzSlotPickerComponent {
  @Input() slots: readonly SlotDisponible[] = [];
  @Input() seleccionada: string | null = null;

  @Output() readonly slotElegido = new EventEmitter<SlotDisponible>();

  claveDe(slot: SlotDisponible): string {
    return claveSlot(slot);
  }
}
