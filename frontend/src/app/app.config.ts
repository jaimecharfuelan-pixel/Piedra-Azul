import { ApplicationConfig, provideZoneChangeDetection } from '@angular/core';
import { provideRouter, withComponentInputBinding } from '@angular/router';
import { provideHttpClient, withFetch, withInterceptors } from '@angular/common/http';

import { routes } from './app.routes';
import { AuthRepositoryPort } from './domain/ports/auth.repository.port';
import { CitaRepositoryPort } from './domain/ports/cita.repository.port';
import { DisponibilidadRepositoryPort } from './domain/ports/disponibilidad.repository.port';
import { EspecialidadRepositoryPort } from './domain/ports/especialidad.repository.port';
import { MedicoRepositoryPort } from './domain/ports/medico.repository.port';
import { PacienteRepositoryPort } from './domain/ports/paciente.repository.port';
import { AuthHttpAdapter } from './infrastructure/http/auth-http.adapter';
import { authTokenInterceptor } from './infrastructure/http/auth-token.interceptor';
import { CitaHttpAdapter } from './infrastructure/http/cita-http.adapter';
import { DisponibilidadHttpAdapter } from './infrastructure/http/disponibilidad-http.adapter';
import { EspecialidadHttpAdapter } from './infrastructure/http/especialidad-http.adapter';
import { MedicoHttpAdapter } from './infrastructure/http/medico-http.adapter';
import { PacienteHttpAdapter } from './infrastructure/http/paciente-http.adapter';

export const appConfig: ApplicationConfig = {
  providers: [
    provideZoneChangeDetection({ eventCoalescing: true }),
    provideRouter(routes, withComponentInputBinding()),
    provideHttpClient(withFetch(), withInterceptors([authTokenInterceptor])),
    { provide: AuthRepositoryPort, useClass: AuthHttpAdapter },
    { provide: CitaRepositoryPort, useClass: CitaHttpAdapter },
    { provide: DisponibilidadRepositoryPort, useClass: DisponibilidadHttpAdapter },
    { provide: EspecialidadRepositoryPort, useClass: EspecialidadHttpAdapter },
    { provide: MedicoRepositoryPort, useClass: MedicoHttpAdapter },
    { provide: PacienteRepositoryPort, useClass: PacienteHttpAdapter },
  ],
};
