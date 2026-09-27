import { Injectable, inject } from '@angular/core';
import { Observable, map } from 'rxjs';
import { AuthRepositoryPort } from '../../domain/ports/auth.repository.port';
import { LoginCommand, SesionUsuario } from '../../domain/models/sesion.model';
import { AuthSessionStore } from '../../infrastructure/auth/auth-session.store';

@Injectable({ providedIn: 'root' })
export class AutenticarUseCase {
  private readonly auth = inject(AuthRepositoryPort);
  private readonly sesion = inject(AuthSessionStore);

  ejecutar(comando: LoginCommand): Observable<SesionUsuario> {
    return this.auth
      .login(comando)
      .pipe(map((dto) => this.sesion.guardarTrasLogin(dto, comando.username)));
  }
}
