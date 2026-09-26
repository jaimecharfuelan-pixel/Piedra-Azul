import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';

import { routes } from './app.routes';
import { CitaRepositoryPort } from './domain/ports/cita.repository.port';
import { CitaHttpAdapter } from './infrastructure/http/cita-http.adapter';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(),
    { provide: CitaRepositoryPort, useClass: CitaHttpAdapter },
  ],
};
