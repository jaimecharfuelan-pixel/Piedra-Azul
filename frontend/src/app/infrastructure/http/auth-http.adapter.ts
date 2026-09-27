import { Injectable, inject } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import {
  LoginCommand,
  RegistroPacienteAuthCommand,
  TokenResponseDto,
  UsuarioResponseDto,
} from '../../domain/models/sesion.model';
import { AuthRepositoryPort } from '../../domain/ports/auth.repository.port';

@Injectable()
export class AuthHttpAdapter extends AuthRepositoryPort {
  private readonly http = inject(HttpClient);
  private readonly baseUrl = '/api/auth';

  login(comando: LoginCommand): Observable<TokenResponseDto> {
    return this.http.post<TokenResponseDto>(`${this.baseUrl}/login`, comando);
  }

  registroPaciente(comando: RegistroPacienteAuthCommand): Observable<UsuarioResponseDto> {
    return this.http.post<UsuarioResponseDto>(`${this.baseUrl}/registro-paciente`, comando);
  }
}
