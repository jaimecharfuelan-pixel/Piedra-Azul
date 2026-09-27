import { Observable } from 'rxjs';
import {
  LoginCommand,
  RegistroPacienteAuthCommand,
  TokenResponseDto,
  UsuarioResponseDto,
} from '../models/sesion.model';

/**
 * Puerto de autenticación (módulo Identidad del backend).
 */
export abstract class AuthRepositoryPort {
  abstract login(comando: LoginCommand): Observable<TokenResponseDto>;

  abstract registroPaciente(comando: RegistroPacienteAuthCommand): Observable<UsuarioResponseDto>;
}
