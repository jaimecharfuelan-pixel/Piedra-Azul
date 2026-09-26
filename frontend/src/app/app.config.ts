import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter } from '@angular/router';
import { provideHttpClient } from '@angular/common/http';

import { routes } from './app.routes';
import { CitaRepositoryPort } from './domain/ports/cita.repository.port';
import { CitaHttpAdapter } from './infrastructure/http/cita-http.adapter';
import { EspecialidadRepositoryPort } from './domain/ports/especialidad.repository.port';
import { EspecialidadHttpAdapter } from './infrastructure/http/especialidad-http.adapter';
import { MedicoRepositoryPort } from './domain/ports/medico.repository.port';
import { MedicoHttpAdapter } from './infrastructure/http/medico-http.adapter';
import { PacienteRepositoryPort } from './domain/ports/paciente.repository.port';
import { PacienteHttpAdapter } from './infrastructure/http/paciente-http.adapter';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes),
    provideHttpClient(),
    { provide: CitaRepositoryPort, useClass: CitaHttpAdapter },
    { provide: EspecialidadRepositoryPort, useClass: EspecialidadHttpAdapter },
    { provide: MedicoRepositoryPort, useClass: MedicoHttpAdapter },
    { provide: PacienteRepositoryPort, useClass: PacienteHttpAdapter },
  ],
};
