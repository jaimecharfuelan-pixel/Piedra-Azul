import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';
import { AuthRepositoryPort } from '../../domain/ports/auth.repository.port';
import { RegistroPacienteAuthCommand, UsuarioResponseDto } from '../../domain/models/sesion.model';

@Injectable({ providedIn: 'root' })
export class RegistrarPacienteAuthUseCase {
  private readonly auth = inject(AuthRepositoryPort);

  ejecutar(comando: RegistroPacienteAuthCommand): Observable<UsuarioResponseDto> {
    return this.auth.registroPaciente(comando);
  }
}
