import { Routes } from '@angular/router';
import { HomePageComponent } from './presentation/pages/home/home.page';
import { CalendarPageComponent } from './presentation/pages/calendar/calendar.page';
import { PersonasPageComponent } from './presentation/pages/personas/personas.page';

export const routes: Routes = [
  { path: '', component: HomePageComponent },
  { path: 'calendario', component: CalendarPageComponent },
  { path: 'personas', component: PersonasPageComponent },
  { path: '**', redirectTo: '' },
];
