import { Routes } from '@angular/router';
import { HomePageComponent } from './presentation/pages/home/home.page';
import { CalendarPageComponent } from './presentation/pages/calendar/calendar.page';

export const routes: Routes = [
  { path: '', component: HomePageComponent },
  { path: 'calendario', component: CalendarPageComponent },
  { path: '**', redirectTo: '' },
];
