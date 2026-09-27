import { Component } from '@angular/core';
import { RouterOutlet } from '@angular/router';
import { PzNavComponent } from './presentation/components/organisms/pz-nav.component';

/**
 * Cascarón de la aplicación: navegación fija arriba y el contenido de la ruta
 * debajo. El enlace de salto permite llegar al contenido sin recorrer el menú.
 */
@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, PzNavComponent],
  template: `
    <a class="app-salto" href="#contenido">Saltar al contenido</a>
    <pz-nav />
    <main id="contenido">
      <router-outlet />
    </main>
  `,
  styles: [
    `
      :host {
        display: block;
        min-height: 100vh;
      }

      .app-salto {
        position: absolute;
        left: -999px;
        top: var(--pz-esp-2);
        z-index: 50;
        padding: var(--pz-esp-2) var(--pz-esp-4);
        background: var(--pz-azul-900);
        color: var(--pz-blanco);
        border-radius: var(--pz-radio);
        text-decoration: none;
      }

      .app-salto:focus {
        left: var(--pz-esp-4);
      }
    `,
  ],
})
export class AppComponent {}
