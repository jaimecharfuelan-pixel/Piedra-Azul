import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withFetch } from '@angular/common/http';

import { routes } from './app.routes';
import { CitaRepositoryPort } from './domain/ports/cita.repository.port';
import { CitaHttpAdapter } from './infrastructure/http/cita-http.adapter';
import { DisponibilidadRepositoryPort } from './domain/ports/disponibilidad.repository.port';
import { DisponibilidadHttpAdapter } from './infrastructure/http/disponibilidad-http.adapter';
import { EspecialidadRepositoryPort } from './domain/ports/especialidad.repository.port';
import { EspecialidadHttpAdapter } from './infrastructure/http/especialidad-http.adapter';
import { MedicoRepositoryPort } from './domain/ports/medico.repository.port';
import { MedicoHttpAdapter } from './infrastructure/http/medico-http.adapter';
import { PacienteRepositoryPort } from './domain/ports/paciente.repository.port';
import { PacienteHttpAdapter } from './infrastructure/http/paciente-http.adapter';

/**
 * Único punto donde los puertos del dominio se atan a un adaptador concreto.
 * Cambiar de REST a otra fuente de datos es cambiar estas líneas.
 */
export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withFetch()),
    { provide: CitaRepositoryPort, useClass: CitaHttpAdapter },
    { provide: DisponibilidadRepositoryPort, useClass: DisponibilidadHttpAdapter },
    { provide: EspecialidadRepositoryPort, useClass: EspecialidadHttpAdapter },
    { provide: MedicoRepositoryPort, useClass: MedicoHttpAdapter },
    { provide: PacienteRepositoryPort, useClass: PacienteHttpAdapter },
  ],
};
