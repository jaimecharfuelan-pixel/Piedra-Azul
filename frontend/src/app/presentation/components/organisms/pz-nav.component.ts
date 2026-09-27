import { Component, computed, inject } from '@angular/core';
import { Router, RouterLink, RouterLinkActive } from '@angular/router';
import { AccionUi, accionesParaRol, rutaPanelPorRol } from '../../../domain/auth/permisos';
import { AuthSessionStore } from '../../../infrastructure/auth/auth-session.store';
import { UiButtonComponent } from '../atoms/ui-button.component';

interface EnlaceNav {
  readonly accion: AccionUi;
  readonly ruta: string;
  readonly texto: string;
  readonly descripcion: string;
}

@Component({
  selector: 'pz-nav',
  standalone: true,
  imports: [RouterLink, RouterLinkActive, UiButtonComponent],
  template: `
    @if (visible()) {
      <header class="pz-nav">
        <div class="pz-nav__interior">
          <a class="pz-nav__marca" [routerLink]="rutaInicio()">
            <span class="pz-nav__logo" aria-hidden="true">PZ</span>
            <span>
              <strong>PiedraAzul</strong>
              <small>{{ etiquetaRol() }}</small>
            </span>
          </a>

          <nav class="pz-nav__enlaces" aria-label="Secciones principales">
            @for (enlace of enlacesVisibles(); track enlace.ruta) {
              <a
                [routerLink]="enlace.ruta"
                routerLinkActive="pz-nav__enlace--activo"
                [routerLinkActiveOptions]="{ exact: enlace.ruta.startsWith('/panel') }"
                [title]="enlace.descripcion"
                class="pz-nav__enlace"
              >
                {{ enlace.texto }}
              </a>
            }
          </nav>

          <div class="pz-nav__sesion">
            <span class="pz-nav__usuario">{{ username() }}</span>
            <ui-button variante="fantasma" tamano="sm" (pulsado)="cerrar()">Salir</ui-button>
          </div>
        </div>
      </header>
    }
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
        flex: 1;
      }

      .pz-nav__enlace {
        display: inline-flex;
        align-items: center;
        padding: var(--pz-esp-2) var(--pz-esp-3);
        border-radius: var(--pz-radio);
        text-decoration: none;
        color: var(--pz-gris-700);
        font-size: var(--pz-texto-sm);
        font-weight: 600;
      }

      .pz-nav__enlace:hover {
        background: var(--pz-azul-50);
        color: var(--pz-azul-700);
      }

      .pz-nav__enlace--activo {
        background: var(--pz-azul-100);
        color: var(--pz-azul-900);
      }

      .pz-nav__sesion {
        display: flex;
        align-items: center;
        gap: var(--pz-esp-2);
      }

      .pz-nav__usuario {
        font-size: var(--pz-texto-sm);
        color: var(--pz-gris-700);
        font-weight: 600;
      }
    `,
  ],
})
export class PzNavComponent {
  private readonly sesion = inject(AuthSessionStore);
  private readonly router = inject(Router);

  readonly visible = this.sesion.autenticado;
  readonly username = this.sesion.username;

  private readonly catalogo: readonly EnlaceNav[] = [
    { accion: 'panel', ruta: '/panel', texto: 'Panel', descripcion: 'Inicio de tu rol' },
    { accion: 'agenda', ruta: '/agenda', texto: 'Agenda', descripcion: 'Citas del día' },
    { accion: 'agendar', ruta: '/agendar', texto: 'Agendar', descripcion: 'Reservar franja' },
    { accion: 'calendario', ruta: '/calendario', texto: 'Calendario', descripcion: 'Vista calendario' },
    { accion: 'configuracion', ruta: '/configuracion', texto: 'Configuración', descripcion: 'Horarios' },
    { accion: 'historial', ruta: '/historial', texto: 'Historial', descripcion: 'Consultas' },
    { accion: 'personas', ruta: '/personas', texto: 'Personas', descripcion: 'Catálogo' },
  ];

  readonly enlacesVisibles = computed(() => {
    const permitidas = new Set(accionesParaRol(this.sesion.rol()));
    const rol = this.sesion.rol();
    return this.catalogo
      .filter((e) => permitidas.has(e.accion))
      .map((e) =>
        e.accion === 'panel' && rol
          ? { ...e, ruta: rutaPanelPorRol(rol) }
          : e
      );
  });

  etiquetaRol(): string {
    return this.sesion.rol() ?? 'Sesión';
  }

  rutaInicio(): string {
    const rol = this.sesion.rol();
    return rol ? rutaPanelPorRol(rol) : '/';
  }

  cerrar(): void {
    this.sesion.cerrarSesion();
    void this.router.navigateByUrl('/');
  }
}
