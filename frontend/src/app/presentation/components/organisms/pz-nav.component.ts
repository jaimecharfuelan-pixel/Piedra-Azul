import { Component } from '@angular/core';
import { RouterLink, RouterLinkActive } from '@angular/router';

interface EnlaceNav {
  readonly ruta: string;
  readonly texto: string;
  readonly descripcion: string;
  readonly icono: 'inicio' | 'agenda' | 'agendar' | 'calendario' | 'config' | 'historial' | 'personas';
}

/**
 * Organismo: barra de navegación del producto.
 *
 * Mantiene visible dónde está el usuario (enlace activo) y ofrece siempre una
 * salida hacia el resto de las pantallas.
 */
@Component({
  selector: 'pz-nav',
  standalone: true,
  imports: [RouterLink, RouterLinkActive],
  template: `
    <header class="pz-nav">
      <div class="pz-nav__interior">
        <a class="pz-nav__marca" routerLink="/">
          <span class="pz-nav__logo" aria-hidden="true">PZ</span>
          <span>
            <strong>PiedraAzul</strong>
            <small>Agendamiento de citas</small>
          </span>
        </a>

        <nav class="pz-nav__enlaces" aria-label="Secciones principales">
          @for (enlace of enlaces; track enlace.ruta) {
            <a
              [routerLink]="enlace.ruta"
              routerLinkActive="pz-nav__enlace--activo"
              [routerLinkActiveOptions]="{ exact: enlace.ruta === '/' }"
              [title]="enlace.descripcion"
              class="pz-nav__enlace"
            >
              <span class="pz-nav__icono" aria-hidden="true">
                @switch (enlace.icono) {
                  @case ('inicio') {
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <path d="M4 11.5 12 4l8 7.5" />
                      <path d="M6 10.5V20h12v-9.5" />
                    </svg>
                  }
                  @case ('agenda') {
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <rect x="4" y="5" width="16" height="15" rx="2" />
                      <path d="M8 3v4M16 3v4M4 10h16M8 14h8" />
                    </svg>
                  }
                  @case ('agendar') {
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <circle cx="12" cy="12" r="8" />
                      <path d="M12 8v8M8 12h8" />
                    </svg>
                  }
                  @case ('calendario') {
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <rect x="3" y="5" width="18" height="16" rx="2" />
                      <path d="M3 10h18M8 3v4M16 3v4" />
                    </svg>
                  }
                  @case ('config') {
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <circle cx="12" cy="12" r="3" />
                      <path d="M19 12a7 7 0 0 0-.2-1.5l1.6-1.2-1.8-3.1-1.9.6A7 7 0 0 0 15 5.2L14.7 3h-5.4L9 5.2A7 7 0 0 0 7.3 6.8l-1.9-.6L3.6 9.3l1.6 1.2A7 7 0 0 0 5 12c0 .5.1 1 .2 1.5l-1.6 1.2 1.8 3.1 1.9-.6A7 7 0 0 0 9 18.8l.3 2.2h5.4l.3-2.2a7 7 0 0 0 1.7-1.6l1.9.6 1.8-3.1-1.6-1.2c.1-.5.2-1 .2-1.5Z" />
                    </svg>
                  }
                  @case ('historial') {
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <path d="M7 4h10a2 2 0 0 1 2 2v14l-3-2-3 2-3-2-3 2V6a2 2 0 0 1 2-2Z" />
                    </svg>
                  }
                  @case ('personas') {
                    <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                      <circle cx="9" cy="8" r="3" />
                      <path d="M4 19c0-3 2.2-5 5-5s5 2 5 5" />
                      <circle cx="17" cy="9" r="2.2" />
                    </svg>
                  }
                }
              </span>
              {{ enlace.texto }}
            </a>
          }
        </nav>
      </div>
    </header>
  `,
  styles: [
    `
      .pz-nav {
        background: var(--pz-blanco);
        border-bottom: var(--pz-borde);
        box-shadow: var(--pz-sombra-sm);
      }

      .pz-nav__interior {
        max-width: var(--pz-ancho-contenido);
        margin: 0 auto;
        padding: var(--pz-esp-3) var(--pz-esp-4);
        display: flex;
        flex-wrap: wrap;
        align-items: center;
        justify-content: space-between;
        gap: var(--pz-esp-4);
      }

      .pz-nav__marca {
        display: flex;
        align-items: center;
        gap: var(--pz-esp-3);
        text-decoration: none;
        color: var(--pz-azul-900);
      }

      .pz-nav__marca small {
        display: block;
        font-size: var(--pz-texto-xs);
        color: var(--pz-gris-500);
        font-weight: 400;
      }

      .pz-nav__logo {
        display: grid;
        place-items: center;
        width: 2.25rem;
        height: 2.25rem;
        border-radius: var(--pz-radio);
        background: var(--pz-azul-700);
        color: var(--pz-blanco);
        font-weight: 700;
        font-size: var(--pz-texto-sm);
      }

      .pz-nav__enlaces {
        display: flex;
        flex-wrap: wrap;
        gap: var(--pz-esp-1);
      }

      .pz-nav__enlace {
        display: inline-flex;
        align-items: center;
        gap: var(--pz-esp-2);
        padding: var(--pz-esp-2) var(--pz-esp-3);
        border-radius: var(--pz-radio);
        text-decoration: none;
        color: var(--pz-gris-700);
        font-size: var(--pz-texto-sm);
        font-weight: 600;
      }

      .pz-nav__icono {
        display: grid;
        place-items: center;
        width: 1.15rem;
        height: 1.15rem;
        color: currentColor;
      }

      .pz-nav__icono svg {
        width: 100%;
        height: 100%;
      }

      .pz-nav__enlace:hover {
        background: var(--pz-azul-50);
        color: var(--pz-azul-700);
      }

      .pz-nav__enlace--activo {
        background: var(--pz-azul-100);
        color: var(--pz-azul-900);
      }
    `,
  ],
})
export class PzNavComponent {
  readonly enlaces: readonly EnlaceNav[] = [
    { ruta: '/', texto: 'Inicio', descripcion: 'Página principal', icono: 'inicio' },
    { ruta: '/agenda', texto: 'Agenda del día', descripcion: 'Citas de un médico en una fecha', icono: 'agenda' },
    { ruta: '/agendar', texto: 'Agendar cita', descripcion: 'Reservar una franja libre', icono: 'agendar' },
    { ruta: '/calendario', texto: 'Calendario', descripcion: 'Vista semanal o mensual de las citas', icono: 'calendario' },
    { ruta: '/configuracion', texto: 'Configuración', descripcion: 'Ventana de reservas y horarios', icono: 'config' },
    { ruta: '/historial', texto: 'Historial', descripcion: 'Consultas ya atendidas', icono: 'historial' },
    { ruta: '/personas', texto: 'Personas', descripcion: 'Médicos, especialidades y pacientes', icono: 'personas' },
  ];
}
