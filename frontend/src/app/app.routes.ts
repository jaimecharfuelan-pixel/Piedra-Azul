import { Routes } from '@angular/router';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { RolUsuario } from './domain/models/rol-usuario.enum';
import { rutaPanelPorRol } from './domain/auth/permisos';
import { AuthSessionStore } from './infrastructure/auth/auth-session.store';
import { authGuard, guestGuard } from './presentation/guards/auth.guard';
import { accionGuard, rolGuard } from './presentation/guards/rol.guard';

const R = RolUsuario;

export const routes: Routes = [
  {
    path: '',
    title: 'Piedra Azul — Centro de salud',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./presentation/pages/landing/landing.page').then((m) => m.LandingPageComponent),
  },
  {
    path: 'login',
    title: 'Iniciar sesión — Piedra Azul',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./presentation/pages/login/login.page').then((m) => m.LoginPageComponent),
  },
  {
    path: 'registro',
    title: 'Crear cuenta — Piedra Azul',
    canActivate: [guestGuard],
    loadComponent: () =>
      import('./presentation/pages/registro/registro.page').then((m) => m.RegistroPageComponent),
  },
  {
    path: 'panel',
    canActivate: [authGuard],
    children: [
      {
        path: '',
        pathMatch: 'full',
        canActivate: [
          () => {
            const rol = inject(AuthSessionStore).rol();
            const router = inject(Router);
            if (!rol) {
              return router.createUrlTree(['/login']);
            }
            return router.createUrlTree([rutaPanelPorRol(rol)]);
          },
        ],
        loadComponent: () =>
          import('./presentation/pages/panel/panel.page').then((m) => m.PanelPageComponent),
      },
      {
        path: 'admin',
        title: 'Panel administrador',
        canActivate: [rolGuard],
        data: { roles: [R.ADMINISTRADOR] },
        loadComponent: () =>
          import('./presentation/pages/panel/panel.page').then((m) => m.PanelPageComponent),
      },
      {
        path: 'agendador',
        title: 'Panel agendador',
        canActivate: [rolGuard],
        data: { roles: [R.AGENDADOR] },
        loadComponent: () =>
          import('./presentation/pages/panel/panel.page').then((m) => m.PanelPageComponent),
      },
      {
        path: 'medico',
        title: 'Panel médico',
        canActivate: [rolGuard],
        data: { roles: [R.MEDICO] },
        loadComponent: () =>
          import('./presentation/pages/panel/panel.page').then((m) => m.PanelPageComponent),
      },
      {
        path: 'paciente',
        title: 'Panel paciente',
        canActivate: [rolGuard],
        data: { roles: [R.PACIENTE] },
        loadComponent: () =>
          import('./presentation/pages/panel/panel.page').then((m) => m.PanelPageComponent),
      },
    ],
  },
  {
    path: 'agenda',
    title: 'Agenda del día — PiedraAzul',
    canActivate: [authGuard, accionGuard],
    data: { accion: 'agenda' },
    loadComponent: () =>
      import('./presentation/pages/agenda/agenda.page').then((m) => m.AgendaPageComponent),
  },
  {
    path: 'agendar',
    title: 'Agendar cita — PiedraAzul',
    canActivate: [authGuard, accionGuard],
    data: { accion: 'agendar' },
    loadComponent: () =>
      import('./presentation/pages/agendar/agendar.page').then((m) => m.AgendarPageComponent),
  },
  {
    path: 'configuracion',
    title: 'Configuración — PiedraAzul',
    canActivate: [authGuard, accionGuard],
    data: { accion: 'configuracion' },
    loadComponent: () =>
      import('./presentation/pages/configuracion/configuracion.page').then(
        (m) => m.ConfiguracionPageComponent
      ),
  },
  {
    path: 'calendario',
    title: 'Calendario — PiedraAzul',
    canActivate: [authGuard, accionGuard],
    data: { accion: 'calendario' },
    loadComponent: () =>
      import('./presentation/pages/calendar/calendar.page').then((m) => m.CalendarPageComponent),
  },
  {
    path: 'historial',
    title: 'Historial — PiedraAzul',
    canActivate: [authGuard, accionGuard],
    data: { accion: 'historial' },
    loadComponent: () =>
      import('./presentation/pages/historial/historial.page').then(
        (m) => m.HistorialPageComponent
      ),
  },
  {
    path: 'personas',
    title: 'Personas — PiedraAzul',
    canActivate: [authGuard, accionGuard],
    data: { accion: 'personas' },
    loadComponent: () =>
      import('./presentation/pages/personas/personas.page').then((m) => m.PersonasPageComponent),
  },
  { path: '**', redirectTo: '' },
];
