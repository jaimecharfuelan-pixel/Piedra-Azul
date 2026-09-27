import { Component } from '@angular/core';
import { RouterLink } from '@angular/router';

type IconoAcceso = 'agenda' | 'agendar' | 'config' | 'calendario' | 'historial' | 'personas';

interface AccesoRapido {
  readonly ruta: string;
  readonly icono: IconoAcceso;
  readonly titulo: string;
  readonly descripcion: string;
}

@Component({
  selector: 'app-home-page',
  standalone: true,
  imports: [RouterLink],
  template: `
    <section class="pz-pagina inicio">
      <header class="pz-encabezado-pagina">
        <h1>PiedraAzul</h1>
        <p>
          Agenda citas médicas y de terapia en línea. Elige un profesional, mira sus horarios
          libres y confirma en unos minutos.
        </p>
      </header>

      <div class="inicio__accesos">
        @for (acceso of accesos; track acceso.ruta) {
          <a class="pz-tarjeta inicio__acceso" [routerLink]="acceso.ruta">
            <span class="inicio__icono" aria-hidden="true">
              @switch (acceso.icono) {
                @case ('agenda') {
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <rect x="4" y="5" width="16" height="15" rx="2" />
                    <path d="M8 3v4M16 3v4M4 10h16" />
                    <path d="M8 14h3M8 17h8" />
                  </svg>
                }
                @case ('agendar') {
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <circle cx="12" cy="12" r="8" />
                    <path d="M12 8v8M8 12h8" />
                  </svg>
                }
                @case ('config') {
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <circle cx="12" cy="12" r="3" />
                    <path
                      d="M19.4 15a1.7 1.7 0 0 0 .3 1.8l.1.1a2 2 0 1 1-2.8 2.8l-.1-.1a1.7 1.7 0 0 0-1.8-.3 1.7 1.7 0 0 0-1 1.5V21a2 2 0 1 1-4 0v-.2a1.7 1.7 0 0 0-1-1.5 1.7 1.7 0 0 0-1.8.3l-.1.1a2 2 0 1 1-2.8-2.8l.1-.1a1.7 1.7 0 0 0 .3-1.8 1.7 1.7 0 0 0-1.5-1H3a2 2 0 1 1 0-4h.2a1.7 1.7 0 0 0 1.5-1 1.7 1.7 0 0 0-.3-1.8l-.1-.1a2 2 0 1 1 2.8-2.8l.1.1a1.7 1.7 0 0 0 1.8.3H9a1.7 1.7 0 0 0 1-1.5V3a2 2 0 1 1 4 0v.2a1.7 1.7 0 0 0 1 1.5 1.7 1.7 0 0 0 1.8-.3l.1-.1a2 2 0 1 1 2.8 2.8l-.1.1a1.7 1.7 0 0 0-.3 1.8V9c.3.6.9 1 1.5 1H21a2 2 0 1 1 0 4h-.2a1.7 1.7 0 0 0-1.5 1Z"
                    />
                  </svg>
                }
                @case ('calendario') {
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <rect x="3" y="5" width="18" height="16" rx="2" />
                    <path d="M3 10h18M8 3v4M16 3v4" />
                    <rect x="7" y="13" width="3" height="3" rx="0.5" />
                    <rect x="14" y="13" width="3" height="3" rx="0.5" />
                  </svg>
                }
                @case ('historial') {
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <path d="M7 4h10a2 2 0 0 1 2 2v14l-3-2-3 2-3-2-3 2V6a2 2 0 0 1 2-2Z" />
                    <path d="M9 9h6M9 13h6" />
                  </svg>
                }
                @case ('personas') {
                  <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8">
                    <circle cx="9" cy="8" r="3" />
                    <path d="M4 19c0-3 2.2-5 5-5s5 2 5 5" />
                    <circle cx="17" cy="9" r="2.2" />
                    <path d="M16.2 14.2c2.2.4 3.8 2 3.8 4.3" />
                  </svg>
                }
              }
            </span>
            <h2 class="inicio__titulo">{{ acceso.titulo }}</h2>
            <p class="inicio__descripcion">{{ acceso.descripcion }}</p>
            <span class="inicio__ir" aria-hidden="true">Entrar →</span>
          </a>
        }
      </div>

      <div class="pz-tarjeta inicio__orden">
        <h2 class="pz-tarjeta__titulo">Para empezar</h2>
        <ol class="inicio__pasos">
          <li>
            En <strong>Personas</strong> registra especialidades, médicos y pacientes.
          </li>
          <li>
            En <strong>Configuración</strong> define el horario de cada médico. Sin horario no hay
            citas que ofrecer.
          </li>
          <li>En <strong>Agendar cita</strong> elige una franja libre y confirma.</li>
          <li>
            En <strong>Agenda del día</strong> consulta, reagenda, atiende o cancela las citas.
          </li>
        </ol>
      </div>
    </section>
  `,
  styles: [
    `
      .inicio {
        display: flex;
        flex-direction: column;
        gap: var(--pz-esp-5);
      }

      .inicio__accesos {
        display: grid;
        gap: var(--pz-esp-4);
        grid-template-columns: repeat(auto-fit, minmax(240px, 1fr));
      }

      .inicio__acceso {
        display: flex;
        flex-direction: column;
        gap: var(--pz-esp-2);
        text-decoration: none;
        color: inherit;
        transition: box-shadow var(--pz-transicion), transform var(--pz-transicion);
      }

      .inicio__acceso:hover {
        box-shadow: var(--pz-sombra);
        transform: translateY(-2px);
      }

      .inicio__icono {
        display: grid;
        place-items: center;
        width: 2.5rem;
        height: 2.5rem;
        border-radius: var(--pz-radio);
        background: var(--pz-azul-100);
        color: var(--pz-azul-700);
      }

      .inicio__icono svg {
        width: 1.4rem;
        height: 1.4rem;
      }

      .inicio__titulo {
        font-size: var(--pz-texto-lg);
      }

      .inicio__descripcion {
        margin: 0;
        flex: 1;
        font-size: var(--pz-texto-sm);
        color: var(--pz-gris-700);
      }

      .inicio__ir {
        font-size: var(--pz-texto-sm);
        font-weight: 600;
        color: var(--pz-azul-700);
      }

      .inicio__pasos {
        margin: 0;
        padding-left: 1.25rem;
        display: flex;
        flex-direction: column;
        gap: var(--pz-esp-2);
        font-size: var(--pz-texto-sm);
        color: var(--pz-gris-700);
      }
    `,
  ],
})
export class HomePageComponent {
  readonly accesos: readonly AccesoRapido[] = [
    {
      ruta: '/agenda',
      icono: 'agenda',
      titulo: 'Agenda del día',
      descripcion: 'Consulta las citas de un médico en una fecha y cambia su estado.',
    },
    {
      ruta: '/agendar',
      icono: 'agendar',
      titulo: 'Agendar cita',
      descripcion: 'Regístrate o identifícate, elige profesional, día y franja libre.',
    },
    {
      ruta: '/configuracion',
      icono: 'config',
      titulo: 'Configuración',
      descripcion: 'Ventana de reservas y horario de atención de cada médico.',
    },
    {
      ruta: '/calendario',
      icono: 'calendario',
      titulo: 'Calendario',
      descripcion: 'Las citas de un médico por semana o por mes, según su estado.',
    },
    {
      ruta: '/historial',
      icono: 'historial',
      titulo: 'Historial',
      descripcion: 'Consultas registradas al marcar una cita como atendida.',
    },
    {
      ruta: '/personas',
      icono: 'personas',
      titulo: 'Personas',
      descripcion: 'Especialidades, médicos y pacientes del centro.',
    },
  ];
}
