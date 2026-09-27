import { Component, computed, inject } from '@angular/core';
import { RouterLink } from '@angular/router';
import { AccionUi, accionesParaRol } from '../../../domain/auth/permisos';
import { RolUsuario } from '../../../domain/models/rol-usuario.enum';
import { AuthSessionStore } from '../../../infrastructure/auth/auth-session.store';

interface AccionPanel {
  readonly accion: AccionUi;
  readonly ruta: string;
  readonly titulo: string;
  readonly descripcion: string;
}

const CATALOGO: readonly AccionPanel[] = [
  {
    accion: 'agenda',
    ruta: '/agenda',
    titulo: 'Agenda del día',
    descripcion: 'Consulta citas; cancela o reagenda según tu rol.',
  },
  {
    accion: 'agendar',
    ruta: '/agendar',
    titulo: 'Agendar cita',
    descripcion: 'Reserva una franja libre con un profesional.',
  },
  {
    accion: 'calendario',
    ruta: '/calendario',
    titulo: 'Calendario',
    descripcion: 'Vista semanal o mensual de las citas.',
  },
  {
    accion: 'configuracion',
    ruta: '/configuracion',
    titulo: 'Configuración',
    descripcion: 'Ventana de reservas y horarios de atención.',
  },
  {
    accion: 'historial',
    ruta: '/historial',
    titulo: 'Historial',
    descripcion: 'Consultas registradas al atender una cita.',
  },
  {
    accion: 'personas',
    ruta: '/personas',
    titulo: 'Personas',
    descripcion: 'Especialidades, médicos y pacientes.',
  },
];

const TITULOS: Record<RolUsuario, string> = {
  [RolUsuario.ADMINISTRADOR]: 'Panel del administrador',
  [RolUsuario.AGENDADOR]: 'Panel del agendador',
  [RolUsuario.MEDICO]: 'Panel del médico',
  [RolUsuario.PACIENTE]: 'Panel del paciente',
};

const SUBTITULOS: Record<RolUsuario, string> = {
  [RolUsuario.ADMINISTRADOR]:
    'Configura el centro, gestiona personas y revisa calendario e historial.',
  [RolUsuario.AGENDADOR]:
    'Agenda citas para cualquier paciente y mantén el día ordenado.',
  [RolUsuario.MEDICO]:
    'Revisa tu agenda, atiende consultas y agenda cuando haga falta.',
  [RolUsuario.PACIENTE]: 'Agenda, consulta tu historial y gestiona tus citas.',
};

@Component({
  selector: 'app-panel-page',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './panel.page.html',
  styleUrl: './panel.page.scss',
})
export class PanelPageComponent {
  private readonly sesion = inject(AuthSessionStore);

  readonly username = this.sesion.username;
  readonly rol = this.sesion.rol;

  readonly titulo = computed(() => {
    const r = this.rol();
    return r ? TITULOS[r] : 'Panel';
  });

  readonly subtitulo = computed(() => {
    const r = this.rol();
    return r ? SUBTITULOS[r] : '';
  });

  readonly acciones = computed(() => {
    const permitidas = new Set(accionesParaRol(this.rol()));
    return CATALOGO.filter((a) => permitidas.has(a.accion));
  });
}
