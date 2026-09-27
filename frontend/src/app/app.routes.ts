import { Routes } from '@angular/router';

/**
 * Rutas de la SPA. Las páginas se cargan de forma diferida para que la portada
 * no arrastre el peso de FullCalendar.
 */
export const routes: Routes = [
  {
    path: '',
    title: 'PiedraAzul — Inicio',
    loadComponent: () =>
      import('./presentation/pages/home/home.page').then((m) => m.HomePageComponent),
  },
  {
    path: 'agenda',
    title: 'Agenda del día — PiedraAzul',
    loadComponent: () =>
      import('./presentation/pages/agenda/agenda.page').then((m) => m.AgendaPageComponent),
  },
  {
    path: 'agendar',
    title: 'Agendar cita — PiedraAzul',
    loadComponent: () =>
      import('./presentation/pages/agendar/agendar.page').then((m) => m.AgendarPageComponent),
  },
  {
    path: 'configuracion',
    title: 'Configuración — PiedraAzul',
    loadComponent: () =>
      import('./presentation/pages/configuracion/configuracion.page').then(
        (m) => m.ConfiguracionPageComponent
      ),
  },
  {
    path: 'calendario',
    title: 'Calendario — PiedraAzul',
    loadComponent: () =>
      import('./presentation/pages/calendar/calendar.page').then((m) => m.CalendarPageComponent),
  },
  {
    path: 'historial',
    title: 'Historial — PiedraAzul',
    loadComponent: () =>
      import('./presentation/pages/historial/historial.page').then(
        (m) => m.HistorialPageComponent
      ),
  },
  {
    path: 'personas',
    title: 'Personas — PiedraAzul',
    loadComponent: () =>
      import('./presentation/pages/personas/personas.page').then((m) => m.PersonasPageComponent),
  },
  { path: '**', redirectTo: '' },
];
